import { useState } from 'react';
import { post } from '../api/rest';
import { messageService } from '../services/messageService';

export default function RawTxSubmit() {
  const [raw, setRaw] = useState('');
  const [busy, setBusy] = useState(false);
  const submit = async () => {
    setBusy(true);
    try {
      const tx = JSON.parse(raw);
      await post('/tx', tx);
      setRaw('');
      messageService.success('Transaction submitted');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Invalid JSON';
      messageService.error(msg);
    } finally {
      setBusy(false);
    }
  };
  return (
    <section className="rounded-lg bg-white shadow p-4 space-y-2" aria-label="raw transaction submit">
      <h2 className="text-lg font-semibold">Submit Raw Transaction</h2>
      <textarea
        aria-label="raw-tx-json"
        className="w-full rounded border-slate-300 p-2 font-mono"
        rows={5}
        value={raw}
        onChange={e => setRaw(e.target.value)}
      />
      <button
        onClick={submit}
        disabled={busy}
        className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
      >
        {busy ? 'Submitting…' : 'Submit'}
      </button>
    </section>
  );
}
