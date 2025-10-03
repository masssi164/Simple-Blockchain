import { DocumentDuplicateIcon } from '@heroicons/react/24/outline';
import QRCode from 'react-qr-code';
import { MineArea } from './MiningArea';
import { Transfer } from './Transfer';
import { useWallet } from '../hooks/useWallet';

export default function WalletView() {
  const { wallet, balance, utxos, isLoading } = useWallet();

  const spendable = balance.toFixed(8);
  const utxoCount = utxos?.length ?? 0;

  return (
    <section className="grid gap-6 md:grid-cols-2">
      {/* Address / QR / Balances ------------------------------------------- */}
      <div className="rounded-lg bg-white shadow p-6">
        <h2 className="mb-2 font-bold">Your address</h2>
        <code aria-label={wallet.address} className="block break-all">
          {wallet.address}
        </code>
        <button
          onClick={() => navigator.clipboard.writeText(wallet.address)}
          className="mt-1 inline-flex items-center rounded-md bg-slate-100 px-2 py-1 text-sm text-slate-700 hover:bg-slate-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2"
        >
          <DocumentDuplicateIcon
            className="mr-1 h-4 w-4"
            aria-hidden="true"
          />
          Copy
        </button>

        <div
          className="mx-auto mt-4 h-44 w-44"
          role="img"
          aria-label="Wallet QR code">
          <QRCode value={wallet.address} size={176} />
        </div>
        <p className="mb-1 mt-4 font-mono text-lg">
          <strong>Spendable:</strong> {isLoading ? '…' : spendable}
        </p>
        <p className="font-mono text-sm text-slate-600">
          UTXOs tracked: {isLoading ? '…' : utxoCount}
        </p>
      </div>

      {/* Mining + Transfer -------------------------------------------------- */}
      <div className="space-y-6">
        <MineArea />
        <Transfer />
      </div>
    </section>
  );
}
