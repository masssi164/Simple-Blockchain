import { useState } from 'react';
import QRCode from 'react-qr-code';
import useSWR from 'swr';
import { get, post } from '../api/rest';

type WalletInfo = { address: string; confirmedBalance: number };
type SendDto    = { recipient: string; amount: number };

export default function WalletView() {
  const { data } = useSWR<WalletInfo>(
    '/wallet',
    (path: string) => get<WalletInfo>(path)
  );

  /* ----- send form state ----- */
  const [recip,  setRecip]  = useState('');
  const [amount, setAmount] = useState(0);

  const doSend = async () => {
    await post<SendDto, unknown>('/wallet/send', { recipient: recip, amount });
    setRecip(''); setAmount(0);
  };

  if (!data) return null;

  return (
    <section className="grid gap-6 md:grid-cols-2">
    {/* Address ---------------------------------------------------- */}
    <div className="p-4 border rounded shadow-sm">
        <h2 className="font-bold mb-2">Your address</h2>

        <code aria-label={data.address} className="block break-all mb-2">
        {data.address}
        </code>
                <button className="btn btn-sm"
                onClick={() => navigator.clipboard.writeText(data.address)}>
          Copy
        </button>

        <div className="mt-4 w-44 h-44 mx-auto">
          <QRCode value={data.address} size={176} aria-label='QrCode adresse' />
        </div>


        {/* NEW ── show confirmed balance */}
        <p className="text-lg font-mono mb-4">
        <strong>Balance:</strong> {data.confirmedBalance.toFixed(2)}
        </p>

    </div>

  {/* Send form -------------------------------------------------- */}
      <form className="p-4 border rounded shadow-sm flex flex-col gap-4"
            onSubmit={e => { e.preventDefault(); doSend(); }}>
        <h2 className="font-bold">Send coins</h2>

        <label>
          <span className="block text-sm">Recipient address</span>
          <input required className="input"
                 value={recip}
                 onChange={e => setRecip(e.target.value)}
                 aria-describedby="addrHelp" />
        </label>
        <p id="addrHelp" className="text-xs text-gray-500">
          Paste the Base-58 address (starts with “1…”)
        </p>

        <label>
          <span className="block text-sm">Amount</span>
          <input required type="number" min="0.00000001" step="any"
                 className="input"
                 value={amount}
                 onChange={e => setAmount(parseFloat(e.target.value))}/>
        </label>

        <button className="btn btn-primary self-start">Send</button>
      </form>
    </section>
  );
}
