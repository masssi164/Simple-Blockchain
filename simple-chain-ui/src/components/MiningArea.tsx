import { useState } from 'react';
import { CpuChipIcon } from '@heroicons/react/24/outline';
import { post } from '../api/rest';
import type { Block } from '../types/block';

// Subcomponent to render block details
function BlockDetails({ block }: { block: Block }) {
  return (
    <div className="mt-6 p-4 bg-white bg-opacity-20 rounded-lg">
      <h3 className="text-xl font-semibold mb-2">Newly Mined Block</h3>
      <ul className="space-y-1 text-sm font-mono">
        <li><strong>Height:</strong> {block.height}</li>
        <li><strong>Difficulty Bits:</strong> {block.compactDifficultyBits}</li>
        <li><strong>Hash:</strong> {block.hashHex}</li>
      </ul>
    </div>
  );
}

export function MineArea() {
  const [block, setBlock] = useState<Block | null>(null);
  const [isMining, setIsMining] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const doMine = async () => {
    setIsMining(true);
    setError(null);
    try {
      const newBlock = await post<Block>('/mining/mine');
      setBlock(newBlock);
    } catch (err: any) {
      setError(err.message ?? 'Unknown error');
    } finally {
      setIsMining(false);
    }
  };

  return (
    <section className="bg-gradient-to-r from-purple-500 to-indigo-600 p-6 rounded-2xl shadow-lg text-white">
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

      {error && (
        <p className="mt-4 text-red-300" role="alert">
          {error}
        </p>
      )}

      {block && <BlockDetails block={block} />}
    </section>
  );
}