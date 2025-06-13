import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RawTxSubmit from '../components/RawTxSubmit';

vi.mock('../api/rest', () => ({
  __esModule: true,
  post: vi.fn(),
}));
vi.mock('../services/messageService', () => ({
  messageService: { success: vi.fn(), error: vi.fn() },
}));

describe('<RawTxSubmit />', () => {
  it('submits parsed JSON to /tx', async () => {
    const { post } = await import('../api/rest');
    render(<RawTxSubmit />);
    fireEvent.change(screen.getByLabelText(/raw-tx-json/i), {
      target: { value: '{"foo":1}' },
    });
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));
    expect(post).toHaveBeenCalledWith('/tx', { foo: 1 });
  });
});
