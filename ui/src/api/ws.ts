export type Listener<T = unknown> = (msg: T) => void;

class NodeWs {
  private ws?: WebSocket;
  private listeners: Listener[] = [];

  connect() {
    this.ws = new WebSocket(import.meta.env.VITE_NODE_WS);
    this.ws.onmessage = ev => {
      let data: unknown;
      try {
        data = JSON.parse(ev.data);
      } catch {
        return;                       // ignorieren bei JSON-Fehler
      }
      this.listeners.forEach(cb => cb(data));
    };
  }

  on<T = unknown>(cb: Listener<T>) { this.listeners.push(cb as Listener); }

close() {
  if (this.ws &&
      (this.ws.readyState === WebSocket.OPEN ||
       this.ws.readyState === WebSocket.CLOSING)) {
    this.ws.close();
  }
  this.ws = undefined;
}
}

export const wsSingleton = new NodeWs();
