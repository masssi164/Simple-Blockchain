// src/components/Transfer.tsx
// ----------------------------------------------------------------------------
//  Transfer modal – sends coins from the local wallet to any valid recipient
//  address by POSTing to the backend REST endpoint. Includes:
//    • Client‑side Base‑58 address validation (same alphabet as BTC)
//    • Safari‑safe <input type="number"> handling by storing the value as a
//      string until submission, then converting to Number
//    • Optimistic SWR cache update so the UI reflects the new balance instantly
//    • Toast feedback (react‑hot‑toast) for success / error cases
// ----------------------------------------------------------------------------

import {
  CloseButton,
  Dialog,
  DialogBackdrop,
  DialogPanel,
  DialogTitle,
  Transition,
} from '@headlessui/react';
import {
  ArrowRightCircleIcon,
  ExclamationTriangleIcon,
  PaperAirplaneIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';
import { messageService } from '../services/messageService';
import { Fragment, useCallback, useState } from 'react';
import { mutate } from 'swr';
import { post } from '../api/rest';

// DTO produced by the WalletController on the backend
export type SendFundsDto = {
  recipient: string;
  amount: number;
};

// Base‑58 alphabet without visually ambiguous characters
const BASE58_REGEX = /^[1-9A-HJ-NP-Za-km-z]{25,40}$/;

/**
 * Transfer‑modal with optimistic SWR cache update and inline success / error
 * messages. Opens via a button and closes itself after submit or cancel.
 */
export function Transfer() {
  /* ------------------------------------------------------------------------ */
  /* Local component state                                                    */
  /* ------------------------------------------------------------------------ */
  const [open, setOpen] = useState(false);
  const [recipient, setRecipient] = useState('');
  const [amountStr, setAmountStr] = useState(''); // keep raw string for Safari
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  /* ------------------------------------------------------------------------ */
  /* Helpers                                                                  */
  /* ------------------------------------------------------------------------ */
  const resetForm = () => {
    setRecipient('');
    setAmountStr('');
    setErrorMsg(null);
  };

  const closeModal = useCallback(() => {
    setOpen(false);
    resetForm();
  }, []);

  /* ------------------------------------------------------------------------ */
  /* Submit handler                                                           */
  /* ------------------------------------------------------------------------ */
  const handleSubmit = useCallback<React.FormEventHandler>(
    async e => {
      e.preventDefault();

      // Basic client‑side validation to avoid pointless round‑trips
      if (!BASE58_REGEX.test(recipient)) {
        setErrorMsg('Recipient address is not a valid Base‑58 string.');
        return;
      }
      const amount = Number(amountStr);
      if (!Number.isFinite(amount) || amount <= 0) {
        setErrorMsg('Please enter an amount greater than zero.');
        return;
      }

      setSubmitting(true);
      setErrorMsg(null);
      try {
        // ---- POST /api/wallet/send ----------------------------------------
        await post<void, SendFundsDto>('/wallet/send', {
          recipient,
          amount,
        });

        /* ------------------------------------------------------------------ */
        /* Optimistic SWR cache update – decrease confirmed balance           */
        /* ------------------------------------------------------------------ */
          mutate(
            '/wallet',
            (current: { confirmedBalance?: number } | undefined) =>
              current && typeof current.confirmedBalance === 'number'
                ? {
                  ...current,
                  confirmedBalance: current.confirmedBalance - amount,
                }
              : current,
          false,
        );
        mutate('/wallet'); // trigger re‑fetch so pending/outgoing shows up

        /* ------------------------------------------------------------------ */
        /* User feedback                                                      */
        /* ------------------------------------------------------------------ */
        messageService.success(
          `Sent ${amount.toFixed(8)} coins to ${recipient}`,
        );

        /*       ✅ Alles ok – Formular zurücksetzen und Modal schließen      */
        closeModal();
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : 'Transaction failed';
        setErrorMsg(msg);
        messageService.error(msg);
      } finally {
        setSubmitting(false);
      }
    },
    [closeModal, recipient, amountStr],
  );

  /* ------------------------------------------------------------------------ */
  /* Render                                                                   */
  /* ------------------------------------------------------------------------ */
  return (
    <>
      {/* Launcher button ---------------------------------------------------- */}
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 font-medium text-white shadow hover:bg-blue-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2"
      >
        <ArrowRightCircleIcon className="mr-2 h-5 w-5" aria-hidden="true" />
        Transfer
      </button>

      {/* Modal -------------------------------------------------------------- */}
      <Transition show={open} as={Fragment}>
        <Dialog
          open={open}
          onClose={closeModal}
          className="fixed inset-0 z-50 overflow-y-auto"
          aria-label="New transfer"
        >
          <DialogBackdrop className="fixed inset-0 bg-black/30" />
          <div className="flex min-h-screen items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4 sm:scale-95"
              enterTo="opacity-100 translate-y-0 sm:scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0 sm:scale-100"
              leaveTo="opacity-0 translate-y-4 sm:scale-95"
            >
              <DialogPanel className="w-full max-w-md transform overflow-hidden rounded-lg bg-white p-6 text-left align-middle shadow-xl transition-all">
                {/* Header --------------------------------------------------- */}
                <div className="flex items-start justify-between">
                  <DialogTitle className="text-lg font-medium text-slate-900">
                    New transfer
                  </DialogTitle>
                  <CloseButton
                    as="button"
                    onClick={closeModal}
                    className="text-slate-400 hover:text-slate-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2"
                  >
                    <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                    <span className="sr-only">Close modal</span>
                  </CloseButton>
                </div>

                {/* Error banner ------------------------------------------- */}
                {errorMsg && (
                  <p
                    className="mt-4 inline-flex items-center rounded-md bg-red-50 px-3 py-2 text-sm text-red-800 ring-1 ring-inset ring-red-600/20"
                    role="alert"
                  >
                    <ExclamationTriangleIcon className="mr-2 h-5 w-5 text-red-600" aria-hidden="true" />
                    {errorMsg}
                  </p>
                )}

                {/* Form ---------------------------------------------------- */}
                <form onSubmit={handleSubmit} className="mt-4 space-y-4">
                  {/* Recipient address */}
                  <label className="block">
                    <span className="text-sm font-medium text-slate-700">Recipient address</span>
                    <input
                      id="transfer-recipient"
                      type="text"
                      required
                      className="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                      value={recipient}
                      onChange={e => setRecipient(e.target.value.trim())}
                    />
                  </label>

                  {/* Amount */}
                  <label className="block">
                    <span className="text-sm font-medium text-slate-700">Amount</span>
                    <input
                      id="transfer-amount"
                      type="number"
                      required
                      min="0.00000001"
                      step="any"
                      className="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                      value={amountStr}
                      onChange={e => setAmountStr(e.target.value)}
                    />
                  </label>

                  {/* Submit button */}
                  <div className="pt-4 text-right">
                    <button
                      type="submit"
                      disabled={submitting || !recipient || !amountStr}
                      className="inline-flex items-center rounded-md bg-green-600 px-4 py-2 font-medium text-white shadow hover:bg-green-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 disabled:opacity-50"
                    >
                      <PaperAirplaneIcon
                        className={`mr-2 h-5 w-5 ${submitting ? 'animate-spin' : ''}`}
                        aria-hidden="true"
                      />
                      {submitting ? 'Sending…' : 'Send'}
                    </button>
                  </div>
                </form>
              </DialogPanel>
            </Transition.Child>
          </div>
        </Dialog>
      </Transition>
    </>
  );
}
