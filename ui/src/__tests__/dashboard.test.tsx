import React from 'react';
import { render, screen } from '@testing-library/react';
import Dashboard from '../pages/Dashboard';

/* gRPC-Call stubben ------------------------------------------------------- */
vi.mock('../api/grpc', () => ({
  chainLatest: vi.fn(() =>
    Promise.resolve({
      height: 7,
      compactDifficultyBits: 0x1f0fffff,
      hashHex: 'deadbeef'.repeat(8),
    }),
  ),
}));

/* WalletView stubben – wir testen hier nur die Chain-Info ----------------- */
vi.mock('../components/WalletView', () => ({
  __esModule: true,
  default: () => <div data-testid="wallet-view" />,
}));
vi.mock('../components/BlockList', () => ({
  __esModule: true,
  default: () => <div data-testid="block-list" />,
}));
vi.mock('../components/BlockHistory', () => ({
  __esModule: true,
  default: () => <div data-testid="block-history" />,
}));
vi.mock('../components/NodeIdBadge', () => ({
  __esModule: true,
  default: () => <div data-testid="node-id" />,
}));

/* useSWR stubben, damit Chain-Info sofort im State ist -------------------- */
vi.mock('swr', () => ({
  __esModule: true,
  default: () => ({
    data: {
      height: 7,
      compactDifficultyBits: 0x1f0fffff,
      hashHex: 'deadbeef'.repeat(8),
    },
  }),
}));

describe('<Dashboard />', () => {
  it('renders chain info cards', () => {
    render(<Dashboard />);

    expect(screen.getByRole('heading', { name: /chain info/i })).toBeInTheDocument();
    expect(screen.getByText(/block height/i)).toBeInTheDocument();
    expect(screen.getByText(/difficulty bits/i)).toBeInTheDocument();
    expect(screen.getByText(/latest hash/i)).toBeInTheDocument();
    expect(screen.getByText('7')).toBeInTheDocument();
    expect(screen.getByTestId('node-id')).toBeInTheDocument();
    expect(screen.getByTestId('wallet-view')).toBeInTheDocument();
    expect(screen.getByTestId('block-list')).toBeInTheDocument();
    expect(screen.getByTestId('block-history')).toBeInTheDocument();
  });
});
