export interface P2PMessage {
  type: 'HandshakeDto' | 'NewBlockDto' | 'NewTxDto' | string;
  rawBlockJson?: string;
  rawTxJson?: string;
}

export type Listener<T = P2PMessage> = (msg: T) => void;

export class NodeWs {
  private ws?: WebSocket;
  private listeners: Listener[] = [];
  private reconnectMs = 1000;
  private closed = false;

  connect() {
    this.closed = false;
    this.open();
  }

  private open() {
    this.ws = new WebSocket(import.meta.env.VITE_NODE_WS);

    this.ws.onopen = () => {
      this.reconnectMs = 1000;
      const hello = {
        type: 'HandshakeDto',
        nodeId: 'ui-client',
        protocolVersion: '0.4.0',
        listenPort: 0,
      };
      this.ws?.send(JSON.stringify(hello));
    };

    this.ws.onmessage = ev => {
      let data: P2PMessage;
      try {
        data = JSON.parse(ev.data);
      } catch {
        return; // ignorieren bei JSON-Fehler
      }
      if (data.type === 'NewBlockDto' || data.type === 'NewTxDto') {
        this.listeners.forEach(cb => cb(data));
      }
    };

    this.ws.onclose = () => {
      if (!this.closed) this.scheduleReconnect();
    };
    this.ws.onerror = () => {
      if (!this.closed) this.scheduleReconnect();
    };
  }

  private scheduleReconnect() {
    this.ws = undefined;
    setTimeout(() => this.open(), this.reconnectMs);
    this.reconnectMs = Math.min(this.reconnectMs * 2, 30000);
  }

  on<T = P2PMessage>(cb: Listener<T>) { this.listeners.push(cb as Listener); }

  close() {
    this.closed = true;
    if (this.ws &&
        (this.ws.readyState === WebSocket.OPEN ||
         this.ws.readyState === WebSocket.CONNECTING ||
         this.ws.readyState === WebSocket.CLOSING)) {
      this.ws.close();
    }
    this.ws = undefined;
  }
}

export const wsSingleton = new NodeWs();
