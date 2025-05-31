import { useEffect } from 'react';
import { wsSingleton } from './api/ws';
import Dashboard from './pages/Dashboard';

export default function App() {
  /* WebSocket-Lifecycle -------------------------------------------------- */
  useEffect(() => {
    wsSingleton.connect();
    return () => wsSingleton.close();
  }, []);

  /* Toast bei neuem Block ------------------------------------------------ */
  useEffect(() => {
    wsSingleton.on<{ type: string; rawBlockJson?: string }>(m => {
      if (m.type === 'NewBlockDto' && m.rawBlockJson) {
        const blk = JSON.parse(m.rawBlockJson);
        console.info('New block', blk.height, blk.hashHex);
        // TODO: react-hot-toast einbauen
      }
    });
  }, []);

  return <Dashboard />;
}
