import { render, screen } from '@testing-library/react';
import BlockList from '../components/BlockList';

vi.mock('../api/grpc', () => ({ chainPage: vi.fn() }));
vi.mock('swr', () => ({
  __esModule: true,
  default: () => ({
    data: [
      { height: 1, compactDifficultyBits: 1, hashHex: 'hash1' },
      { height: 2, compactDifficultyBits: 1, hashHex: 'hash2' },
    ],
  }),
}));

describe('<BlockList />', () => {
  it('renders recent blocks', () => {
    render(<BlockList />);
    expect(screen.getByRole('heading', { name: /recent blocks/i })).toBeInTheDocument();
    expect(screen.getByText('#2')).toBeInTheDocument();
  });
});
