import { render, screen } from '@testing-library/react';
import App from '../App';

vi.mock('../pages/Dashboard', () => ({
  __esModule: true,
  default: () => <div data-testid="dashboard" />,
}));

vi.mock('./../api/p2p', () => ({
  p2pSingleton: { connect: vi.fn(), close: vi.fn(), on: vi.fn() },
}));
vi.mock('../services/messageService', () => ({
  messageService: { success: vi.fn(), error: vi.fn() },
}));

it('renders app header', () => {
  render(<App />);
  expect(screen.getByRole('heading', { name: /simple blockchain/i })).toBeInTheDocument();
  expect(screen.getByTestId('dashboard')).toBeInTheDocument();
});
