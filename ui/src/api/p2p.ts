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
import type {
  Transport,
  ConnectionEncrypter,
  StreamMuxerFactory,
  Stream,
} from '@libp2p/interface';
import type { Multiaddr as Libp2pMultiaddr } from '@libp2p/interface/node_modules/@multiformats/multiaddr';

export interface P2PMessage {
  type: 'HandshakeDto' | 'NewBlockDto' | 'NewTxDto' | string;
  rawBlockJson?: string;
  rawTxJson?: string;
}

export type Listener<T = P2PMessage> = (msg: T) => void;

export class NodeP2P {
  private node?: Libp2p;
  private stream?: any;
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private listeners: Listener[] = [];
  private reconnectMs = 1000;
  private closed = false;
  private readonly jwt = import.meta.env.VITE_NODE_JWT_SECRET
    ? sign({}, import.meta.env.VITE_NODE_JWT_SECRET)
    : undefined;
  private factory: typeof createLibp2p = createLibp2p;

  private withComponents<T>(fn: () => T): (components: any) => T {
    return () => fn();
  }

  async connect(factory: typeof createLibp2p = createLibp2p) {
    this.closed = false;
    this.factory = factory;
    try {
      await this.open(factory);
    } catch {
      this.scheduleReconnect();
    }
  }

  private async open(factory: typeof createLibp2p) {
    if (this.node) await this.node.stop();
    const opts: Parameters<typeof createLibp2p>[0] = {
      transports: [
        this.withComponents(() => new TCP() as unknown as Transport),
      ],
      connectionEncrypters: [
        this.withComponents(() => new Noise() as unknown as ConnectionEncrypter),
      ],
      streamMuxers: [
        this.withComponents(() => new Mplex() as unknown as StreamMuxerFactory),
      ],
    };
    this.node = await factory(opts);
    await this.node.start();
    const addr = import.meta.env.VITE_NODE_LIBP2P;
    try {
      this.stream = await this.node.dialProtocol(
        multiaddr(addr) as unknown as Libp2pMultiaddr,
        '/simple-blockchain/1.0.0'
      );
      await this.sendHandshake();
      this.reconnectMs = 1000;
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = undefined;
      }
      this.readLoop();
    } catch {
      this.scheduleReconnect();
    }
  }

  private async sendHandshake() {
    const root: any = p2p.de.flashyotter.blockchain_node;
    const Handshake = root.p2p.Handshake;
    const P2PMessage = root.p2p.P2PMessage;
      const hs = Handshake.create({
        nodeId: 'ui-client',
        peerId: this.node!.peerId.toString(),
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
      const root: any = p2p.de.flashyotter.blockchain_node;
      const P2PMessage = root.p2p.P2PMessage;
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
                rawTxJson: root.grpc.Transaction.toObject(msg.newTx!.tx, {
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
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = setTimeout(() => {
      this.open(this.factory).catch(() => this.scheduleReconnect());
    }, this.reconnectMs);
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
