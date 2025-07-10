import useSWR from 'swr';
import { chainPage } from '../api/grpc';
import type { Block } from '../types/block';

export default function BlockList() {
  const { data } = useSWR<Block[]>(
    '/chain?from=0',
    () => chainPage(0, 20),
    { refreshInterval: 10000 },
  );

  if (!data) return null;

  return (
    <section aria-label="recent blocks" className="space-y-2">
      <h2 className="text-lg font-semibold">Recent Blocks</h2>
      <ul className="space-y-1">
        {data
          .slice(-5)
          .reverse()
          .map(b => (
            <li key={b.hashHex} className="rounded bg-white p-2 shadow">
              <span className="font-mono">#{b.height}</span>{' '}
              <span className="font-mono">{b.hashHex.slice(0, 16)}…</span>
            </li>
          ))}
      </ul>
    </section>
  );
}
