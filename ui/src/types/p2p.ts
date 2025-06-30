export interface P2PMessage {
  type: 'HandshakeDto' | 'NewBlockDto' | 'NewTxDto' | string;
  rawBlockJson?: string;
  rawTxJson?: string;
}
