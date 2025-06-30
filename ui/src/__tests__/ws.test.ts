import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NodeWs } from '../api/ws';

class MockWebSocket {
  static instances: MockWebSocket[] = [] as MockWebSocket[];
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSING = 2;
  static CLOSED = 3;

  onopen: ((ev: Event) => void) | null = null;
  onmessage: ((ev: MessageEvent) => void) | null = null;
  onclose: ((ev: CloseEvent) => void) | null = null;
  onerror: ((ev: Event) => void) | null = null;
  readyState = MockWebSocket.CONNECTING;
  send = vi.fn();
  close = vi.fn(() => {
    this.readyState = MockWebSocket.CLOSED;
    this.onclose && this.onclose({} as CloseEvent);
  });
  constructor(public url: string) {
    MockWebSocket.instances.push(this);
  }
}

beforeEach(() => {
  MockWebSocket.instances.length = 0;
  (global as any).WebSocket = MockWebSocket;
});

describe('NodeWs', () => {
  it('sends handshake on open', () => {
    const ws = new NodeWs();
    ws.connect();
    const inst = MockWebSocket.instances[0];
    inst.onopen && inst.onopen(new Event('open'));
    expect(inst.send).toHaveBeenCalledWith(
      JSON.stringify({
        type: 'HandshakeDto',
        nodeId: 'ui-client',
        protocolVersion: '0.4.0',
        listenPort: 0,
      }),
    );
  });

  it('reconnects after close', () => {
    vi.useFakeTimers();
    const ws = new NodeWs();
    ws.connect();
    const first = MockWebSocket.instances[0];
    first.onopen && first.onopen(new Event('open'));
    first.onclose && first.onclose({} as CloseEvent);
    vi.advanceTimersByTime(1000);
    expect(MockWebSocket.instances.length).toBe(2);
    vi.useRealTimers();
  });

  it('forwards only block and tx messages', () => {
    const ws = new NodeWs();
    const handler = vi.fn();
    ws.on(handler);
    ws.connect();
    const inst = MockWebSocket.instances[0];
    inst.onopen && inst.onopen(new Event('open'));
    inst.onmessage && inst.onmessage({ data: JSON.stringify({ type: 'NewBlockDto', rawBlockJson: '{}' }) } as MessageEvent);
    inst.onmessage && inst.onmessage({ data: JSON.stringify({ type: 'PeerListDto', peers: [] }) } as MessageEvent);
    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler.mock.calls[0][0].type).toBe('NewBlockDto');
  });
});
