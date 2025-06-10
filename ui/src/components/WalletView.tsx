import { DocumentDuplicateIcon } from '@heroicons/react/24/outline';
import QRCode from 'react-qr-code';
import useSWR from 'swr';
import { get } from '../api/rest';
import { MineArea } from './MiningArea';
import { Transfer } from './Transfer';

type WalletInfo = {
  address: string;
  confirmedBalance: number;
  pendingOutgoing?: number;
  pendingIncoming?: number;
};

export default function WalletView() {
  const { data } = useSWR<WalletInfo>(
    '/wallet',
    (path: string) => get<WalletInfo>(path),
    { refreshInterval: 5_000 },
  );

  if (!data) return null;

  const available =
    data.confirmedBalance -
    (data.pendingOutgoing ?? 0) +
    (data.pendingIncoming ?? 0);

  return (
    <section className="grid gap-6 md:grid-cols-2">
      {/* Address / QR / Balances ------------------------------------------- */}
      <div className="rounded-lg bg-white shadow p-6">
        <h2 className="mb-2 font-bold">Your address</h2>
        <code aria-label={data.address} className="block break-all">
          {data.address}
        </code>
        <button
          onClick={() => navigator.clipboard.writeText(data.address)}
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
          <QRCode value={data.address} size={176} />
        </div>
        <p className="mb-1 mt-4 font-mono text-lg">
          <strong>Confirmed:</strong> {data.confirmedBalance.toFixed(8)}
        </p>

        {typeof data.pendingOutgoing === 'number' ||
        typeof data.pendingIncoming === 'number' ? (
          <>
            <p className="mb-1 font-mono text-lg">
              <strong>Pending - out:</strong>{' '}
              {(data.pendingOutgoing ?? 0).toFixed(8)}
            </p>
            <p className="mb-1 font-mono text-lg">
              <strong>Pending + in:</strong>{' '}
              {(data.pendingIncoming ?? 0).toFixed(8)}
            </p>
            <p className="font-mono text-lg">
              <strong>Available:</strong> {available.toFixed(8)}
            </p>
          </>
        ) : null}
      </div>

      {/* Mining + Transfer -------------------------------------------------- */}
      <div className="space-y-6">
        <MineArea />
        <Transfer />
      </div>
    </section>
  );
}
