import { render, screen } from '@testing-library/react';
import WalletView from '../components/WalletView';

const mockedUseWallet = vi.hoisted(() => vi.fn());

vi.mock('../hooks/useWallet', () => ({
  useWallet: mockedUseWallet,
}));

it('shows balances and QR code', () => {
  mockedUseWallet.mockReturnValue({
    wallet: { address: 'addr', publicKeyBase64: 'pk', privateKeyHex: 'dead' },
    balance: 10.5,
    utxos: [{ id: 'a', value: 10.5, recipientAddress: 'addr' }],
    isLoading: false,
    refresh: vi.fn(),
    send: vi.fn(),
  });

  render(<WalletView />);

  expect(screen.getByText('addr')).toBeInTheDocument();
  expect(screen.getByText(/10\.50000000/)).toBeInTheDocument();
  expect(
    screen.getByRole('img', { name: /wallet qr code/i }),
  ).toBeInTheDocument();
});
