import type { Block } from '../types/block';
import { chainLatest } from './grpc';

export type NodeEvent =
  | { type: 'block'; block: Block };

export type NodeEventListener = (event: NodeEvent) => void;

/**
 * Lightweight polling-based event stream. The backend currently exposes
 * gRPC endpoints but no dedicated WebSocket feed, so we emulate a
 * push channel by periodically fetching the latest block and emitting an
 * event whenever the height or hash changes. dApp clients can reuse the same
 * surface without depending on libp2p internals.
 */
class NodeEventStream {
  private timer?: ReturnType<typeof setTimeout>;
  private listeners = new Set<NodeEventListener>();
  private lastHash?: string;
  private lastHeight?: number;
  private running = false;
  private backoffMs = 2000;

  constructor(private readonly fetchLatest: () => Promise<Block> = chainLatest) {}

  start() {
    if (this.running) return;
    this.running = true;
    this.backoffMs = 2000;
    this.schedule(0);
  }

  stop() {
    this.running = false;
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = undefined;
    }
  }

  on(listener: NodeEventListener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private emit(event: NodeEvent) {
    for (const listener of this.listeners) {
      listener(event);
    }
  }

  private schedule(delay: number) {
    if (!this.running) return;
    this.timer = setTimeout(() => void this.poll(), delay);
  }

  private async poll() {
    if (!this.running) return;
    try {
      const block = await this.fetchLatest();
      const hashChanged = block.hashHex && block.hashHex !== this.lastHash;
      const heightChanged =
        typeof block.height === 'number' && block.height !== this.lastHeight;
      if (hashChanged || heightChanged) {
        this.lastHash = block.hashHex;
        this.lastHeight = block.height;
        this.emit({ type: 'block', block });
      }
      this.backoffMs = 2000;
    } catch (error) {
      console.warn('Failed to poll latest block', error);
      this.backoffMs = Math.min(this.backoffMs * 2, 30000);
    } finally {
      this.schedule(this.backoffMs);
    }
  }
}

export const nodeEvents = new NodeEventStream();
