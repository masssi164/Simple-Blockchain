export interface Block {
  height: number;
  compactDifficultyBits: number;
  hashHex: string;
  txList?: unknown[];
}
