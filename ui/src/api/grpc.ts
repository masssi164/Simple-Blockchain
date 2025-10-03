import * as $protobuf from "protobufjs/minimal";
import { sign } from 'jsonwebtoken';
import { de } from '../services/node_pb';
import type { Block } from '../types/block';

const DEFAULT_GRPC_PORT = '9090';

const jwt = import.meta.env.VITE_NODE_JWT_SECRET ? sign({}, import.meta.env.VITE_NODE_JWT_SECRET) : undefined;

const headers = {
  'Content-Type': 'application/grpc+proto',
  ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
};

function normalizeEnvHost(raw?: string | null): string | undefined {
  if (!raw) {
    return undefined;
  }
  const trimmed = raw.trim();
  return trimmed.length > 0 ? trimmed : undefined;
}

export function resolveGrpcHost(
  envHost: string | null | undefined = import.meta.env?.VITE_NODE_GRPC,
  location: Pick<Location, 'hostname'> | undefined = typeof window !== 'undefined' ? window.location : undefined,
): string {
  const normalizedEnvHost = normalizeEnvHost(envHost ?? undefined);
  if (normalizedEnvHost) {
    return normalizedEnvHost;
  }

  const hostname = normalizeEnvHost(location?.hostname ?? undefined);
  if (hostname) {
    return `${hostname}:${DEFAULT_GRPC_PORT}`;
  }

  return `localhost:${DEFAULT_GRPC_PORT}`;
}

export function createRpcImpl(
  fetchImpl: typeof fetch = fetch,
  envHost: string | null | undefined = import.meta.env?.VITE_NODE_GRPC,
  location: Pick<Location, 'hostname'> | undefined = typeof window !== 'undefined' ? window.location : undefined,
): $protobuf.RPCImpl {
  const host = resolveGrpcHost(envHost, location);

  return (method, requestData, callback) => {
    const m = method as any;
    // @ts-ignore accessing internal protobufjs fields
    fetchImpl(`http://${host}/${m.service.fullName}/${m.name}`, {
      method: 'POST',
      headers,
      body: requestData,
    })
      .then(async res => {
        if (!res.ok) throw new Error(res.statusText);
        return new Uint8Array(await res.arrayBuffer());
      })
      .then(data => callback(null, data))
      .catch(err => callback(err));
  };
}

// Generic rpc implementation using protobufjs service stubs
const rpcImpl = createRpcImpl();

const Mining = de.flashyotter.blockchain_node.grpc.Mining;
const Wallet = de.flashyotter.blockchain_node.grpc.Wallet;
const Chain = de.flashyotter.blockchain_node.grpc.Chain;

const mining = Mining.create(rpcImpl, false, false);
const wallet = Wallet.create(rpcImpl, false, false);
const chain = Chain.create(rpcImpl, false, false);

export async function mineBlock(): Promise<Block> {
  const b = await mining.mine({});
  return toBlock(b);
}

export async function sendFunds(recipient: string, amount: number) {
  return wallet.send({ recipient, amount });
}

export async function walletInfo() {
  const info = await wallet.info({});
  return { address: info.address, confirmedBalance: info.balance };
}

export async function chainLatest(): Promise<Block> {
  const b = await chain.latest({});
  return toBlock(b);
}

export async function chainPage(page: number, size: number): Promise<Block[]> {
  const list = await chain.page({ page, size });
  return list.blocks.map(toBlock);
}

function toBlock(b: any): Block {
  return {
    height: b.height,
    compactDifficultyBits: b.compactBits,
    hashHex: (b as any).hashHex ?? '',
    txList: b.txList,
  };
}
