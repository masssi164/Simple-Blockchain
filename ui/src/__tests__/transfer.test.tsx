import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Transfer } from '../components/Transfer';
import { vi } from 'vitest';
import type { Mock } from 'vitest';

// Message-Service-Mock für alle Tests
vi.mock('../services/messageService', () => ({
  messageService: { success: vi.fn(), error: vi.fn() },
}));

// gRPC-Modul mocken – einheitliches Mock-Objekt
vi.mock('../api/grpc', () => ({
  __esModule: true,
  sendFunds: vi.fn(), // wird pro Test konfiguriert
}));

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
    const { sendFunds } = await import('../api/grpc');
    (sendFunds as Mock).mockResolvedValueOnce(undefined);

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
    expect(sendFunds).toHaveBeenCalledWith(
      '17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem',
      3.1415,
    );
  });
});
