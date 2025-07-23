import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NodeP2P } from '../api/p2p';
import { de } from '../services/p2p_pb';

const root = de.flashyotter.blockchain_node.p2p;

function emptyAsync() {
  return {
    async *[Symbol.asyncIterator]() {
      return;
    },
  };
}

describe('NodeP2P', () => {
  let dialProtocol: any;
  let start: any;
  let stop: any;

  beforeEach(() => {
    dialProtocol = vi.fn(() => Promise.resolve({ stream: { source: emptyAsync(), sink: vi.fn() } }));
    start = vi.fn();
    stop = vi.fn();
  });

  it('dials protocol on connect', async () => {
    const factory = vi.fn(async () => ({ start, stop, peerId: { toString: () => 'id' }, dialProtocol } as any));
    const p2p = new NodeP2P();
    await p2p.connect(factory);
    expect(factory).toHaveBeenCalled();
    expect(dialProtocol).toHaveBeenCalledWith(expect.anything(), '/simple-blockchain/1.0.0');
  });

  it('reconnects on failure', async () => {
    vi.useFakeTimers();
    const failing = vi.fn(async () => { throw new Error('fail'); });
    const p2p = new NodeP2P();
    p2p.connect(failing);
    await vi.runOnlyPendingTimersAsync();
    vi.useRealTimers();
  });
});
