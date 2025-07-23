// @ts-nocheck
import { createLibp2p, Libp2p } from 'libp2p';
import { TCP } from '@libp2p/tcp';
import { Mplex } from '@libp2p/mplex';
import { Noise } from '@chainsafe/libp2p-noise';
import { multiaddr } from '@multiformats/multiaddr';
import { pipe } from 'it-pipe';
import lp from 'it-length-prefixed';
import * as $protobuf from 'protobufjs/minimal';
import * as p2p from '../services/p2p_pb';
import { sign } from 'jsonwebtoken';

export interface P2PMessage {
  type: 'HandshakeDto' | 'NewBlockDto' | 'NewTxDto' | string;
  rawBlockJson?: string;
  rawTxJson?: string;
}

export type Listener<T = P2PMessage> = (msg: T) => void;

export class NodeP2P {
  private node?: Libp2p;
  private stream?: any;
  private listeners: Listener[] = [];
  private reconnectMs = 1000;
  private closed = false;
  private readonly jwt = import.meta.env.VITE_NODE_JWT_SECRET
    ? sign({}, import.meta.env.VITE_NODE_JWT_SECRET)
    : undefined;
  private factory: typeof createLibp2p = createLibp2p;

  async connect(factory: typeof createLibp2p = createLibp2p) {
    this.closed = false;
    this.factory = factory;
    await this.open(factory);
  }

  private async open(factory: typeof createLibp2p) {
    if (this.node) await this.node.stop();
    this.node = await factory({
      transports: [() => new TCP()],
      connectionEncrypters: [() => new Noise()],
      streamMuxers: [() => new Mplex()],
    });
    await this.node.start();
    const addr = import.meta.env.VITE_NODE_LIBP2P;
    try {
      const { stream } = await (this.node.dialProtocol(
        multiaddr(addr) as any,
        '/simple-blockchain/1.0.0'
      ) as any);
      this.stream = stream;
      await this.sendHandshake();
      this.readLoop();
    } catch {
      this.scheduleReconnect();
    }
  }

  private async sendHandshake() {
    const root = p2p.de.flashyotter.blockchain_node.p2p;
    const Handshake = root.Handshake;
    const P2PMessage = root.P2PMessage;
    const hs = Handshake.create({
      nodeId: 'ui-client',
      peerId: this.node.peerId.toString(),
      protocolVersion: '1.0.0',
      listenPort: 0,
      restPort: 0,
    });
    let msg = P2PMessage.create({ handshake: hs });
    if (this.jwt) msg.jwt = this.jwt;
    const buffer = P2PMessage.encode(msg).finish();
    await pipe([buffer], lp.encode(), this.stream);
  }

  private async readLoop() {
    if (!this.stream) return;
    try {
      const root = p2p.de.flashyotter.blockchain_node.p2p;
      const P2PMessage = root.P2PMessage;
      for await (const buf of pipe(this.stream.source, lp.decode())) {
        const msg = P2PMessage.decode($protobuf.Reader.create(buf));
        if (msg.newBlock || msg.newTx) {
          const dto: P2PMessage = msg.newBlock
            ? {
                type: 'NewBlockDto',
                rawBlockJson: root.grpc.Block.toObject(msg.newBlock.block, {
                  json: true,
                }) as any,
              }
            : {
                type: 'NewTxDto',
                rawTxJson: root.grpc.Transaction.toObject(msg.newTx.tx, {
                  json: true,
                }) as any,
              };
          this.listeners.forEach(cb => cb(dto));
        }
      }
    } catch {
      if (!this.closed) this.scheduleReconnect();
    }
  }

  private scheduleReconnect() {
    this.stream = undefined;
    setTimeout(() => this.open(this.factory), this.reconnectMs);
    this.reconnectMs = Math.min(this.reconnectMs * 2, 30000);
  }

  on<T = P2PMessage>(cb: Listener<T>) {
    this.listeners.push(cb as Listener);
  }

  async close() {
    this.closed = true;
    if (this.stream) {
      await this.stream.close?.();
      this.stream = undefined;
    }
    if (this.node) await this.node.stop();
  }
}

export const p2pSingleton = new NodeP2P();
