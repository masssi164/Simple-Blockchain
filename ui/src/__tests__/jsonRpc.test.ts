import { describe, expect, it, vi } from 'vitest';
import { createRpcCaller, resolveRpcUrl } from '../api/jsonRpc';

describe('jsonRpc helper', () => {
  it('resolves override and falls back to window location', () => {
    expect(resolveRpcUrl('https://node.example/rpc')).toBe('https://node.example/rpc');
    expect(
      resolveRpcUrl('', { protocol: 'https:', hostname: 'chain.local', port: '8443' } as any),
    ).toBe('https://chain.local:8443/rpc');
    expect(resolveRpcUrl(undefined, undefined)).toBe('http://localhost:3333/rpc');
  });

  it('createRpcCaller posts JSON-RPC payload and returns result', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      statusText: 'OK',
      json: async () => ({ jsonrpc: '2.0', id: 1, result: 42 }),
    });

    const call = createRpcCaller(fetchMock as any, 'http://rpc.test');
    const result = await call<number>('eth_blockNumber');

    expect(result).toBe(42);
    expect(fetchMock).toHaveBeenCalledWith(
      'http://rpc.test/rpc',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      }),
    );
  });
});
