import useSWR from 'swr';
import { chainLatest } from '../api/grpc';
import type { Block } from '../types/block';   // ❶ Type-only-Import
import { StatCard } from '../components/StatCard';
import WalletView from '../components/WalletView';
import BlockList from '../components/BlockList';
import BlockHistory from '../components/BlockHistory';
import NodeIdBadge from '../components/NodeIdBadge';

export default function Dashboard() {
  const { data: tip } = useSWR<Block>(
    '/chain/latest',
    () => chainLatest(),
    { refreshInterval: 10_000 },
  );

  return (
    <main className="mx-auto max-w-6xl px-4 py-6 grid gap-6 md:grid-cols-3">
      <div className="md:col-span-3">
        <NodeIdBadge />
      </div>
      <section className="space-y-4" aria-label="chain info">
        <h2 className="text-lg font-semibold">Chain info</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-1">
          <StatCard label="Block height" value={tip?.height ?? '…'} />
          <StatCard label="Difficulty bits" value={tip?.compactDifficultyBits ?? '…'} />
          <StatCard label="Latest hash" value={tip ? tip.hashHex.slice(0, 16) : '…'} />
        </div>
        <BlockList />
        <BlockHistory />
      </section>
      <div className="md:col-span-2 space-y-6">
        <WalletView />
      </div>
    </main>
  );
}
