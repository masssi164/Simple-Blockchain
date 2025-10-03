import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import useSWR from 'swr';
import { fetchUtxos, submitTransaction, type UtxoEntry } from '../api/node';
import {
  buildSignedTransaction,
  ensureWallet,
  type LocalWallet,
  type TransactionPayload,
} from '../lib/wallet';

interface WalletContextValue {
  wallet: LocalWallet;
  utxos?: UtxoEntry[];
  balance: number;
  isLoading: boolean;
  refresh: () => Promise<UtxoEntry[] | undefined>;
  send: (recipient: string, amount: number) => Promise<TransactionPayload>;
}

const WalletContext = createContext<WalletContextValue | undefined>(undefined);

export function WalletProvider({ children }: { children: React.ReactNode }) {
  const [wallet] = useState<LocalWallet>(() => ensureWallet());
  const key = useMemo(() => (wallet.address ? ['wallet-utxo', wallet.address] as const : null), [wallet.address]);

  const { data, mutate, isLoading } = useSWR<UtxoEntry[] | undefined>(
    key,
    () => fetchUtxos(wallet.address),
    { refreshInterval: 10_000 },
  );

  const balance = useMemo(() => data?.reduce((sum, entry) => sum + entry.value, 0) ?? 0, [data]);

  const refresh = useCallback(() => mutate(), [mutate]);

  const send = useCallback(async (recipient: string, amount: number) => {
    if (!data) {
      throw new Error('UTXO set not loaded yet');
    }
    const tx = buildSignedTransaction(wallet, data, recipient, amount);
    await submitTransaction(tx);
    await mutate();
    return tx;
  }, [data, mutate, wallet]);

  const value = useMemo<WalletContextValue>(() => ({
    wallet,
    utxos: data,
    balance,
    isLoading,
    refresh,
    send,
  }), [wallet, data, balance, isLoading, refresh, send]);

  return (
    <WalletContext.Provider value={value}>
      {children}
    </WalletContext.Provider>
  );
}

export function useWallet(): WalletContextValue {
  const ctx = useContext(WalletContext);
  if (!ctx) {
    throw new Error('useWallet must be used within a WalletProvider');
  }
  return ctx;
}
