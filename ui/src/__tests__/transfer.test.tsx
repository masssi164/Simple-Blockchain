import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Transfer } from '../components/Transfer';
import { vi } from 'vitest';

// Message-Service-Mock für alle Tests
vi.mock('../services/messageService', () => ({
  messageService: { success: vi.fn(), error: vi.fn() },
}));

const { mockedUseWallet, mockedSend } = vi.hoisted(() => ({
  mockedUseWallet: vi.fn(),
  mockedSend: vi.fn(),
}));

vi.mock('../hooks/useWallet', () => ({
  useWallet: mockedUseWallet,
}));

beforeEach(() => {
  mockedSend.mockResolvedValue({});
  mockedUseWallet.mockReturnValue({
    wallet: { address: 'myAddress' },
    balance: 10,
    utxos: [],
    isLoading: false,
    refresh: vi.fn(),
    send: mockedSend,
  });
});

afterEach(() => {
  mockedSend.mockReset();
  mockedUseWallet.mockReset();
});

describe('<Transfer />', () => {
  it('blockiert Submit bei ungültiger Empfänger-Adresse', async () => {
    render(<Transfer />);

    // Modal öffnen
    await userEvent.click(screen.getByRole('button', { name: /transfer/i }));
    expect(
      await screen.findByRole('dialog', { name: /new transfer/i })
    ).toBeInTheDocument();

    // Ungültige Daten eingeben & Abschicken
    await userEvent.type(
      screen.getByLabelText(/recipient address/i),
      'NOTBASE58'
    );
    await userEvent.type(screen.getByLabelText(/amount/i), '1');
    await userEvent.click(screen.getByRole('button', { name: /^send$/i }));

    // Error-Banner erscheint
    expect(await screen.findByRole('alert')).toHaveTextContent(
      /not a valid base/i
    );
  });

  it('schließt Modal nach erfolgreichem Senden & ruft API korrekt auf', async () => {
    // --- Mock konfigurieren ---
    render(<Transfer />);
    await userEvent.click(screen.getByRole('button', { name: /transfer/i }));

    // Gültige Daten eingeben
    await userEvent.type(
      screen.getByLabelText(/recipient address/i),
      '17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem'
    );
    await userEvent.type(screen.getByLabelText(/amount/i), '3.1415');
    await userEvent.click(screen.getByRole('button', { name: /^send$/i }));

    // Modal verschwindet
    await waitFor(() =>
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    );

    // API-Aufruf korrekt
    expect(mockedSend).toHaveBeenCalledWith(
      '17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem',
      3.1415,
    );
  });
});
