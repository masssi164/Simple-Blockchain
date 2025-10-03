import type { TransactionPayload } from '../lib/wallet';

const DEFAULT_API = 'http://localhost:3333/api';

function normalize(raw?: string | null): string | undefined {
  if (!raw) return undefined;
  const trimmed = raw.trim();
  return trimmed.length > 0 ? trimmed : undefined;
}

function resolveApiBase(
  envHost: string | null | undefined = import.meta.env?.VITE_NODE_URL,
  location?: Pick<Location, 'protocol' | 'hostname' | 'port'>,
): string {
  const override = normalize(envHost);
  if (override) {
    return override.endsWith('/api')
      ? override.replace(/\/?$/, '')
      : `${override.replace(/\/$/, '')}/api`;
  }

  const hasLocationArg = arguments.length >= 2;
  const resolvedLocation = hasLocationArg
    ? location
    : typeof window !== 'undefined'
      ? (window.location as Pick<Location, 'protocol' | 'hostname' | 'port'>)
      : undefined;

  if (resolvedLocation) {
    const protocol = normalize(resolvedLocation.protocol) ?? 'http:';
    const host = normalize(resolvedLocation.hostname) ?? 'localhost';
    const port = normalize(resolvedLocation.port);
    const portPart = port ? `:${port}` : '';
    return `${protocol}//${host}${portPart}/api`;
  }

  return DEFAULT_API;
}

const apiBase = resolveApiBase();

export interface UtxoEntry {
  id: string;
  value: number;
  recipientAddress: string;
}

async function request<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const response = await fetch(input, init);
  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(text || `${response.status} ${response.statusText}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function fetchUtxos(address: string): Promise<UtxoEntry[]> {
  const url = new URL(`${apiBase}/utxo`);
  url.searchParams.set('address', address);
  return request<UtxoEntry[]>(url.toString(), {
    headers: { Accept: 'application/json' },
  });
}

export async function submitTransaction(tx: TransactionPayload): Promise<void> {
  await request<void>(`${apiBase}/tx`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(tx),
  });
}
