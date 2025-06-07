import { useEffect } from 'react';
import { mutate } from 'swr';
import toast, { Toaster } from 'react-hot-toast';
import { CheckBadgeIcon } from '@heroicons/react/24/solid';
import { wsSingleton } from './api/ws';
import Dashboard from './pages/Dashboard';

/**
 * Root component – keeps the SWR cache in sync with node events
 * and provides global toast notifications.
 */
export default function App() {
  /* ------------------------------------------------------------------ */
  /* WebSocket lifecycle                                                */
  /* ------------------------------------------------------------------ */
  useEffect(() => {
    wsSingleton.connect();
    return () => wsSingleton.close();
  }, []);

  /* ------------------------------------------------------------------ */
  /* React on new blocks                                                */
  /* ------------------------------------------------------------------ */
  useEffect(() => {
    wsSingleton.on<{ type: string; rawBlockJson?: string }>(m => {
      if (m.type === 'NewBlockDto' && m.rawBlockJson) {
        const blk = JSON.parse(m.rawBlockJson);
        console.info('New block', blk.height, blk.hashHex);

        mutate('/chain/latest');
        mutate('/wallet');

        toast.custom(
          t => (
            <div
              className={`${
                t.visible ? 'animate-enter' : 'animate-leave'
              } pointer-events-auto flex w-full max-w-sm rounded-lg bg-slate-800 p-4 shadow-lg ring-1 ring-black ring-opacity-5`}
              role="status"
            >
              <CheckBadgeIcon
                className="h-6 w-6 shrink-0 text-green-400"
                aria-hidden="true"
              />
              <div className="ml-3 flex-1 text-sm text-white">
                New block #{blk.height} accepted
              </div>
            </div>
          ),
          { duration: 4000 },
        ); /* :contentReference[oaicite:0]{index=0} */
      }
    });
  }, []);

  return (
    <>
      <Dashboard />
      <Toaster position="top-right" reverseOrder={false} />
    </>
  );
}
