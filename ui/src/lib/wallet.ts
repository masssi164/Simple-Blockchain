import {
  derivePublicKey,
  privateKeyFromHex,
  privateKeyToHex,
  publicKeyToSpki,
  randomPrivateKey,
  signMessage,
  spkiToAddress,
  spkiToBase64,
} from './crypto';
import type { UtxoEntry } from '../api/node';

const STORAGE_KEY = 'simple-chain:wallet-privkey';

export interface LocalWallet {
  privateKey: Uint8Array;
  privateKeyHex: string;
  publicKey: Uint8Array;
  publicKeySpki: Uint8Array;
  publicKeyBase64: string;
  address: string;
}

export interface TxInputPayload {
  referencedOutputId: string;
  sender: string;
  signature: string;
}

export interface TxOutputPayload {
  value: number;
  recipientAddress: string;
}

export interface TransactionPayload {
  inputs: TxInputPayload[];
  outputs: TxOutputPayload[];
  maxFee?: number;
  tip?: number;
}

function roundToEightDecimals(value: number): number {
  return Math.round(value * 1e8) / 1e8;
}

function loadStoredPrivateKey(): string | null {
  if (typeof window === 'undefined') return null;
  try {
    return window.localStorage.getItem(STORAGE_KEY);
  } catch (error) {
    console.warn('Failed to read wallet from storage', error);
    return null;
  }
}

function storePrivateKey(hex: string): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(STORAGE_KEY, hex);
  } catch (error) {
    console.warn('Failed to persist wallet', error);
  }
}

function deriveWalletFromPrivateKey(hex: string): LocalWallet {
  const privateKey = privateKeyFromHex(hex);
  const publicKey = derivePublicKey(privateKey);
  const publicKeySpki = publicKeyToSpki(publicKey);
  const publicKeyBase64 = spkiToBase64(publicKeySpki);
  const address = spkiToAddress(publicKeySpki);
  return { privateKey, privateKeyHex: hex, publicKey, publicKeySpki, publicKeyBase64, address };
}

export function ensureWallet(): LocalWallet {
  const existing = loadStoredPrivateKey();
  if (existing) {
    return deriveWalletFromPrivateKey(existing);
  }
  const priv = randomPrivateKey();
  const hex = privateKeyToHex(priv);
  storePrivateKey(hex);
  return deriveWalletFromPrivateKey(hex);
}

export function walletFromPrivateKeyHex(hex: string): LocalWallet {
  storePrivateKey(hex);
  return deriveWalletFromPrivateKey(hex);
}

export function clearStoredWallet(): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.removeItem(STORAGE_KEY);
  } catch (error) {
    console.warn('Failed to clear wallet store', error);
  }
}

export function buildSignedTransaction(
  wallet: Pick<LocalWallet, 'privateKey' | 'publicKeyBase64' | 'address'>,
  utxo: UtxoEntry[],
  recipient: string,
  amount: number,
): TransactionPayload {
  if (amount <= 0 || !Number.isFinite(amount)) {
    throw new Error('Amount must be a positive number');
  }

  const inputs: UtxoEntry[] = [];
  let gathered = 0;
  for (const entry of utxo) {
    inputs.push(entry);
    gathered += entry.value;
    if (gathered + 1e-9 >= amount) break;
  }

  if (gathered + 1e-9 < amount) {
    throw new Error('Insufficient funds');
  }

  const change = roundToEightDecimals(gathered - amount);
  const outputs: TxOutputPayload[] = [
    { value: roundToEightDecimals(amount), recipientAddress: recipient },
  ];

  if (change > 1e-8) {
    outputs.push({ value: change, recipientAddress: wallet.address });
  }

  const tx: TransactionPayload = {
    inputs: inputs.map(entry => ({
      referencedOutputId: entry.id,
      sender: wallet.publicKeyBase64,
      signature: '',
    })),
    outputs,
    maxFee: 0,
    tip: 0,
  };

  tx.inputs = tx.inputs.map(input => ({
    ...input,
    signature: signMessage(wallet.privateKey, input.referencedOutputId),
  }));

  return tx;
}
