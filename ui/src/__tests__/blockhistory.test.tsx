import { render, screen } from '@testing-library/react';
import BlockHistory from '../components/BlockHistory';

vi.mock('../api/rest', () => ({ get: vi.fn() }));
vi.mock('swr', () => ({
  __esModule: true,
  default: () => ({
    data: [
      { height: 1, compactDifficultyBits: 1, hashHex: 'h1', txList: [] },
      { height: 2, compactDifficultyBits: 1, hashHex: 'h2', txList: [] },
    ],
  }),
}));

describe('<BlockHistory />', () => {
  it('renders paginated block list', () => {
    render(<BlockHistory />);
    expect(screen.getByRole('heading', { name: /block history/i })).toBeInTheDocument();
    expect(screen.getByText('#2')).toBeInTheDocument();
  });
});
