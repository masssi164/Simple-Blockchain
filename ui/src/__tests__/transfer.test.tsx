// __tests__/transfer.test.tsx - aktualisiert
// ---------------------------------------------------------------------------
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Transfer } from '../components/Transfer';

/* ------------------------------------------------------------------ */
/* REST‑Modul mocken – EIN Objekt exportieren, damit alle Tests dieselbe
   Mock‑Funktion verwenden können.                                    */
/* ------------------------------------------------------------------ */
vi.mock('../api/rest', () => ({
  __esModule: true,
  post: vi.fn(), // wird je Test konfiguriert
}));

describe('<Transfer />', () => {
  it('blockiert Submit bei ungültiger Empfänger‑Adresse', async () => {
    render(<Transfer />);

    // Modal öffnen
    await userEvent.click(screen.getByRole('button', { name: /transfer/i }));
    expect(await screen.findByRole('dialog', { name: /new transfer/i }))
      .toBeInTheDocument();

    // Ungültige Daten eingeben & abschicken
    await userEvent.type(
      screen.getByLabelText(/recipient address/i),
      'NOTBASE58',
    );
    await userEvent.type(screen.getByLabelText(/amount/i), '1');
    await userEvent.click(screen.getByRole('button', { name: /^send$/i }));

    // Error‑Banner erscheint (einfach nach dem ersten Alert suchen)
    expect(await screen.findByRole('alert')).toHaveTextContent(/not a valid base/i);
  });

  it('schließt Modal nach erfolgreichem Senden & ruft API korrekt auf', async () => {
    /* -------------------------------------------------------------- */
    /* Mock konfigurieren                                             */
    /* -------------------------------------------------------------- */
    const { post } = await import('../api/rest');
    (post as vi.Mock).mockResolvedValueOnce(undefined);

    render(<Transfer />);
    await userEvent.click(screen.getByRole('button', { name: /transfer/i }));

    // Gültige Daten eingeben
    await userEvent.type(
      screen.getByLabelText(/recipient address/i),
      '17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem',
    );
    await userEvent.type(screen.getByLabelText(/amount/i), '3.1415');
    await userEvent.click(screen.getByRole('button', { name: /^send$/i }));

    // Modal verschwindet (Formular wurde zurückgesetzt & geschlossen)
    await waitFor(() =>
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument(),
    );

    // API‑Aufruf korrekt
    expect(post).toHaveBeenCalledWith('/wallet/send', {
      recipient: '17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem',
      amount: 3.1415,
    });
  });
});
