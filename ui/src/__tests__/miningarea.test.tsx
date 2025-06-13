import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MineArea } from '../components/MiningArea';

vi.mock('../services/messageService', () => ({
  messageService: { success: vi.fn(), error: vi.fn() },
}));

vi.mock('../api/rest', () => ({
  post: vi.fn(() =>
    Promise.resolve({
      height: 42,
      compactDifficultyBits: 0x1f0fffff,
      hashHex: 'abcd'.repeat(16),
    }),
  ),
}));

describe('<MineArea />', () => {
  it('mines and shows new block info', async () => {
    render(<MineArea />);

    await userEvent.click(
      screen.getByRole('button', { name: /start mining/i }),
    );

    expect(
      await screen.findByText(/newly mined block/i),
    ).toBeInTheDocument();
    expect(screen.getByText(/height:/i)).toHaveTextContent('42');
  });
});
