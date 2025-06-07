import React from 'react';
import { render, screen } from '@testing-library/react';
import Dashboard from '../pages/Dashboard';

/* REST-Call stubben ------------------------------------------------------- */
vi.mock('../api/rest', () => ({
  get: vi.fn(() =>
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

    expect(screen.getByText(/block height/i)).toBeInTheDocument();
    expect(screen.getByText('7')).toBeInTheDocument();
  });
});
