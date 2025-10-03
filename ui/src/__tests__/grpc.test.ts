import { describe, expect, it, vi } from 'vitest';
import { createRpcImpl, resolveGrpcHost } from '../api/grpc';

describe('gRPC helper', () => {
  it('normalizes env host and falls back to defaults', () => {
    expect(resolveGrpcHost('  node.example:8080 ', undefined)).toBe('node.example:8080');
    expect(resolveGrpcHost('', { hostname: 'test.local' } as any)).toBe('test.local:9090');
    expect(resolveGrpcHost(undefined, undefined)).toBe('localhost:9090');
  });

  it('createRpcImpl uses fallback host when env missing', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      arrayBuffer: async () => new ArrayBuffer(0),
    });
    const rpc = createRpcImpl(fetchMock as any, '', undefined);

    const method = { service: { fullName: 'de.flashyotter.blockchain_node.grpc.Chain' }, name: 'Latest' };
    rpc(method as any, new Uint8Array(), () => {});

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:9090/de.flashyotter.blockchain_node.grpc.Chain/Latest',
      expect.objectContaining({ method: 'POST' }),
    );
  });
});
