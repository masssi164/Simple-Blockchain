import { render, screen } from '@testing-library/react';
import NodeIdBadge from '../components/NodeIdBadge';

vi.mock('swr', () => ({
  __esModule: true,
  default: () => ({ data: 'node-abc' }),
}));

describe('<NodeIdBadge />', () => {
  it('shows node id', () => {
    render(<NodeIdBadge />);
    expect(screen.getByText('node-abc')).toBeInTheDocument();
  });
});
