import { render, screen } from '@testing-library/react';
import { StatCard } from '../components/StatCard';

describe('<StatCard />', () => {
  it('renders label & value', () => {
    render(<StatCard label="Foo" value={123} />);
    expect(screen.getByText('Foo')).toBeInTheDocument();
    expect(screen.getByText('123')).toBeInTheDocument();
  });
});
