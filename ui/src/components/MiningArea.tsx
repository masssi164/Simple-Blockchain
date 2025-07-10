import { useState } from 'react';
import { CpuChipIcon } from '@heroicons/react/24/outline';
import { mineBlock } from '../api/grpc';
import { messageService } from '../services/messageService';
import type { Block } from '../types/block';

// Subcomponent to render block details
// …unverändert…
function BlockDetails({ block }: { block: Block }) {
  return (
    <div className="mt-6 rounded-lg bg-white/20 p-4">
      <h3 className="mb-2 text-xl font-semibold">Newly Mined Block</h3>
      <ul className="space-y-1 text-sm font-mono">
        <li><strong>Height: {block.height}</strong></li>
        <li><strong>Difficulty Bits:</strong> {block.compactDifficultyBits}</li>
        <li><strong>Hash:</strong> {block.hashHex}</li>
      </ul>
    </div>
  );
}

export function MineArea() {
  const [block, setBlock] = useState<Block | null>(null);
  const [isMining, setIsMining] = useState(false);

  const doMine = async () => {
    setIsMining(true);
    try {
      const newBlock = await mineBlock();
      setBlock(newBlock);
      messageService.success(`Mined block #${newBlock.height}`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Unknown error';
      messageService.error(msg);
    } finally {
      setIsMining(false);
    }
  };

  return (
    <section className="rounded-xl bg-gradient-to-r from-purple-600 to-indigo-700 p-6 text-white shadow-lg">
      <h2 className="text-2xl font-bold mb-4">Mine a New Block</h2>
      <button
        onClick={doMine}
        disabled={isMining}
        className="flex items-center justify-center px-6 py-3 bg-white text-indigo-600 font-semibold rounded-full shadow-md hover:bg-indigo-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-white disabled:opacity-50 disabled:cursor-not-allowed transform hover:-translate-y-1 transition"
        aria-busy={isMining}
      >
        <CpuChipIcon className={`h-6 w-6 mr-2 ${isMining ? 'animate-spin' : ''}`} aria-hidden="true" />
        <span>{isMining ? 'Mining...' : 'Start Mining'}</span>
      </button>

      {block && <BlockDetails block={block} />}
    </section>
  );
}
