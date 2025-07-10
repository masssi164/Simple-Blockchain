import * as $protobuf from "protobufjs/minimal";
import { sign } from 'jsonwebtoken';
import { de } from '../services/node_pb';

const host = import.meta.env.VITE_NODE_GRPC;
const jwt = import.meta.env.VITE_NODE_JWT_SECRET ? sign({}, import.meta.env.VITE_NODE_JWT_SECRET) : undefined;

const headers = {
  'Content-Type': 'application/grpc+proto',
  ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
};

// Generic rpc implementation using protobufjs service stubs
const rpcImpl: $protobuf.RPCImpl = (method, requestData, callback) => {
  const m = method as any;
  fetch(`http://${host}/${m.service.fullName}/${m.name}`, {
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

const Mining = de.flashyotter.blockchain_node.grpc.Mining;
const Wallet = de.flashyotter.blockchain_node.grpc.Wallet;
const Chain = de.flashyotter.blockchain_node.grpc.Chain;

const mining = Mining.create(rpcImpl, false, false);
const wallet = Wallet.create(rpcImpl, false, false);
const chain = Chain.create(rpcImpl, false, false);

export async function mineBlock() {
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

export async function chainLatest() {
  const b = await chain.latest({});
  return toBlock(b);
}

export async function chainPage(page: number, size: number) {
  const list = await chain.page({ page, size });
  return list.blocks.map(toBlock);
}

function toBlock(b: any) {
  return {
    height: b.height,
    compactDifficultyBits: b.compactBits,
    hashHex: (b as any).hashHex ?? '',
    txList: b.txList,
  };
}
