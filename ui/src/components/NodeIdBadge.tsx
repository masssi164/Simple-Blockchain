import useSWR from 'swr';
import { ServerStackIcon } from '@heroicons/react/24/outline';

const base = (import.meta.env.VITE_NODE_URL || '').replace(/\/api$/, '');

async function fetchNodeId() {
  const res = await fetch(`${base}/node/id`);
  if (!res.ok) throw new Error(res.statusText);
  const data = await res.json();
  return data.nodeId as string;
}

export default function NodeIdBadge() {
  const { data } = useSWR<string>('/node/id', fetchNodeId);
  if (!data) return null;
  return (
    <div className="flex items-center text-sm text-slate-700" aria-label="node-id">
      <ServerStackIcon className="mr-1 h-5 w-5 text-slate-500" aria-hidden="true" />
      <span className="font-mono">{data}</span>
    </div>
  );
}
