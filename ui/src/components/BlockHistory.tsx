import { useState } from 'react';
import useSWR from 'swr';
import { get } from '../api/rest';
import type { Block } from '../types/block';

export default function BlockHistory() {
  const [page, setPage] = useState(0);
  const [gap, setGap] = useState(5);
  const { data } = useSWR<Block[]>(
    `/chain/page?page=${page}&size=${gap}`,
    (path: string) => get<Block[]>(path),
  );

  if (!data) return null;

  return (
    <section aria-label="block history" className="space-y-2">
      <h2 className="text-lg font-semibold">Block History</h2>
      <div className="space-x-2">
        <button
          onClick={() => setPage(p => Math.max(0, p - 1))}
          disabled={page === 0}
          className="rounded bg-blue-600 px-2 py-1 text-white disabled:opacity-50"
        >
          Newer
        </button>
        <button
          onClick={() => setPage(p => p + 1)}
          disabled={data.length < gap}
          className="rounded bg-blue-600 px-2 py-1 text-white disabled:opacity-50"
        >
          Older
        </button>
        <input
          type="number"
          className="w-16 rounded border px-1 py-0.5"
          value={gap}
          onChange={e => setGap(Number(e.target.value))}
          min={1}
        />
      </div>
      <ul className="space-y-1">
        {data
          .slice()
          .reverse()
          .map(b => (
            <li key={b.hashHex} className="rounded bg-white p-2 shadow">
              <details>
                <summary className="cursor-pointer">
                  <span className="font-mono">#{b.height}</span>{' '}
                  <span className="font-mono">{b.hashHex.slice(0, 16)}…</span>
                </summary>
                {b.txList && (
                  <pre className="mt-2 overflow-x-auto text-xs">
                    {JSON.stringify(b.txList, null, 2)}
                  </pre>
                )}
              </details>
            </li>
          ))}
      </ul>
    </section>
  );
}
