import { useEffect } from 'react';
import { mutate } from 'swr';
import { Toaster } from 'react-hot-toast';
import { messageService } from './services/messageService';
import { nodeEvents } from './api/ws';
import Dashboard from './pages/Dashboard';
import { useWallet } from './hooks/useWallet';

/**
 * Root component – keeps the SWR cache in sync with node events
 * and provides global toast notifications.
 */
export default function App() {
  const { refresh } = useWallet();
  /* ------------------------------------------------------------------ */
  /* Event stream lifecycle                                             */
  /* ------------------------------------------------------------------ */
  useEffect(() => {
    nodeEvents.start();
    return () => {
      nodeEvents.stop();
    };
  }, []);

  /* ------------------------------------------------------------------ */
  /* React on new blocks                                                */
  /* ------------------------------------------------------------------ */
  useEffect(() => {
    const unsubscribe = nodeEvents.on(event => {
      if (event.type === 'block') {
        const blk = event.block;
        console.info('New block', blk.height, blk.hashHex);

        mutate('/chain/latest');
        refresh().catch(err => console.warn('Failed to refresh wallet', err));
        if (typeof blk.height === 'number') {
          messageService.success(`New block #${blk.height} accepted`);
        } else {
          messageService.success('New block accepted');
        }
      }
    });

    return unsubscribe;
  }, [refresh]);

  return (
    <>
      <header className="bg-indigo-600 text-white shadow">
        <div className="mx-auto max-w-6xl p-4">
          <h1 className="text-2xl font-semibold">Simple Blockchain</h1>
        </div>
      </header>
      <Dashboard />
      <Toaster position="top-right" reverseOrder={false} />
    </>
  );
}
