import { render, screen } from '@testing-library/react';
import App from '../App';

const mockedUseWallet = vi.hoisted(() => vi.fn());

vi.mock('../hooks/useWallet', () => ({
  useWallet: mockedUseWallet,
}));

vi.mock('../pages/Dashboard', () => ({
  __esModule: true,
  default: () => <div data-testid="dashboard" />,
}));

vi.mock('../api/ws', () => ({
  nodeEvents: { start: vi.fn(), stop: vi.fn(), on: vi.fn(() => () => {}) },
}));
vi.mock('../services/messageService', () => ({
  messageService: { success: vi.fn(), error: vi.fn() },
}));

it('renders app header', () => {
  mockedUseWallet.mockReturnValue({
    wallet: {
      address: 'addr',
      privateKey: new Uint8Array(32),
      privateKeyHex: '00',
      publicKey: new Uint8Array(65),
      publicKeySpki: new Uint8Array(91),
      publicKeyBase64: 'base64',
    },
    balance: 0,
    utxos: [],
    isLoading: false,
    refresh: vi.fn().mockResolvedValue([]),
    send: vi.fn(),
  });
  render(<App />);
  expect(screen.getByRole('heading', { name: /simple blockchain/i })).toBeInTheDocument();
  expect(screen.getByTestId('dashboard')).toBeInTheDocument();
});
