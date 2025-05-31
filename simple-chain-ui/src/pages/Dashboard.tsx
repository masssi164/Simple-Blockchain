import useSWR from 'swr';
import { get } from '../api/rest';
import type { Block } from '../types/block';   // ❶ Type-only-Import
import { StatCard } from '../components/StatCard';

export default function Dashboard() {
  const { data: tip } = useSWR<Block>(
    '/chain/latest',
    path => get<Block>(path),
    { refreshInterval: 10_000 },
  );

  return (
    <main className="max-w-6xl mx-auto p-4 grid gap-4 md:grid-cols-3">
      <StatCard label="Block height" value={tip?.height ?? '…'} />
      <StatCard label="Difficulty bits" value={tip?.compactDifficultyBits ?? '…'} />
      <StatCard label="Latest hash" value={tip ? tip.hashHex.slice(0, 16) : '…'} />
    </main>
  );
}
