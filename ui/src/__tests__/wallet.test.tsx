import { render, screen } from '@testing-library/react';
import WalletView from '../components/WalletView';

/* SWR und gRPC stubs ------------------------------------------------------ */
vi.mock('swr', () => ({
  __esModule: true,
  default: () => ({
    data: { address: 'addr', confirmedBalance: 10.5 },
  }),
}));

vi.mock('../api/grpc', () => ({ walletInfo: vi.fn() }));

it('shows balances and QR code', () => {
  render(<WalletView />);

  expect(screen.getByText('addr')).toBeInTheDocument();
  expect(screen.getByText(/10\.50000000/)).toBeInTheDocument();
  expect(
    screen.getByRole('img', { name: /wallet qr code/i }),
  ).toBeInTheDocument();
});
