import type { Block } from '../types/block';

type FetchLike = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;

const COIN_SCALE = 100_000_000n; // 1 coin = 1e8 base units

function normalize(raw?: string | null): string | undefined {
  if (!raw) return undefined;
  const trimmed = raw.trim();
  return trimmed.length > 0 ? trimmed : undefined;
}

export function resolveRpcUrl(
  envHost: string | null | undefined = import.meta.env?.VITE_NODE_RPC_HTTP,
  location?: Pick<Location, 'protocol' | 'hostname' | 'port'>,
): string {
  const override = normalize(envHost);
  if (override) {
    return override.endsWith('/rpc') ? override : `${override.replace(/\/$/, '')}/rpc`;
  }

  const hasLocationArg = arguments.length >= 2;
  const resolvedLocation = hasLocationArg
    ? location
    : typeof window !== 'undefined'
      ? (window.location as Pick<Location, 'protocol' | 'hostname' | 'port'>)
      : undefined;

  if (resolvedLocation) {
    const protocol = normalize(resolvedLocation.protocol) ?? 'http:';
    const port = normalize(resolvedLocation.port);
    const host = normalize(resolvedLocation.hostname) ?? 'localhost';
    const portPart = port ? `:${port}` : '';
    return `${protocol}//${host}${portPart}/rpc`;
  }

  return 'http://localhost:3333/rpc';
}

export function createRpcCaller(
  fetchImpl: FetchLike = fetch,
  envHost: string | null | undefined = import.meta.env?.VITE_NODE_RPC_HTTP,
  location?: Pick<Location, 'protocol' | 'hostname' | 'port'>,
) {
  let nextId = 1;
  const url = resolveRpcUrl(envHost, location);

  return async function call<T>(method: string, params: unknown[] = []): Promise<T> {
    const response = await fetchImpl(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ jsonrpc: '2.0', id: nextId++, method, params }),
    });

    if (!response.ok) {
      throw new Error(`${response.status} ${response.statusText}`);
    }

    const payload = await response.json();
    if (payload?.error) {
      const message = payload.error?.message ?? 'JSON-RPC error';
      throw new Error(message);
    }
    return payload.result as T;
  };
}

const callRpc = createRpcCaller();

export type WalletInfo = {
  address: string;
  confirmedBalance: number;
  pendingIncoming: number;
  pendingOutgoing: number;
};

function toQuantity(amount: number): string {
  if (!Number.isFinite(amount) || amount < 0) {
    throw new Error('Amount must be a finite positive number');
  }
  const scaled = BigInt(Math.round(amount * Number(COIN_SCALE)));
  return `0x${scaled.toString(16)}`;
}

export async function mineBlock(): Promise<Block> {
  return callRpc<Block>('sb_mineBlock');
}

export async function sendFunds(recipient: string, amount: number): Promise<void> {
  await callRpc('eth_sendTransaction', [{ to: recipient, value: toQuantity(amount) }]);
}

export async function walletInfo(): Promise<WalletInfo> {
  return callRpc<WalletInfo>('sb_walletInfo');
}

export async function chainLatest(): Promise<Block> {
  return callRpc<Block>('sb_chainLatest');
}

export async function chainPage(page: number, size: number): Promise<Block[]> {
  return callRpc<Block[]>('sb_chainPage', [page, size]);
}
