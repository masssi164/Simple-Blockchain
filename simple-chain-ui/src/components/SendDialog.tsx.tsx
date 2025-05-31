import { Dialog, DialogBackdrop } from '@headlessui/react';   // ❶ KEIN DialogBackdrop import
import { useState } from 'react';
import { post } from '../api/rest';

interface Props {
  isOpen: boolean;
  onClose(): void;
}

export function SendDialog({ isOpen, onClose }: Props) {
  const [recipient, setRecipient] = useState('');
  const [amount, setAmount] = useState(0);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    await post('/wallet/send', { recipient, amount });
    onClose();
  }

  return (
    <Dialog open={isOpen} onClose={onClose} className="fixed inset-0 z-50 grid place-items-center">
      <DialogBackdrop className="fixed inset-0 bg-black/50" />

      <form
        onSubmit={onSubmit}
        className="relative z-10 max-w-md mx-auto bg-white p-6 rounded-2xl shadow space-y-4"
      >
        <Dialog.Title className="text-xl font-semibold">Send funds</Dialog.Title>

        <label className="block">
          <span className="sr-only">Recipient public key (base64)</span>
          <input
            type="text"
            required
            value={recipient}
            onChange={e => setRecipient(e.target.value)}
            placeholder="Recipient (base64)"
            className="w-full rounded-lg border-slate-300"
          />
        </label>

        <label className="block">
          <span className="sr-only">Amount</span>
          <input
            type="number"
            required
            min="0"
            step="any"
            value={amount}
            onChange={e => setAmount(parseFloat(e.target.value))}
            className="w-full rounded-lg border-slate-300"
          />
        </label>

        <button type="submit" className="w-full rounded-lg bg-indigo-600 text-white py-2">
          Send
        </button>
      </form>
    </Dialog>
  );
}
