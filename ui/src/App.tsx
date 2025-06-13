import { useEffect } from 'react';
import { mutate } from 'swr';
import { Toaster } from 'react-hot-toast';
import { messageService } from './services/messageService';
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
        messageService.success(`New block #${blk.height} accepted`);
      }
    });
  }, []);

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
