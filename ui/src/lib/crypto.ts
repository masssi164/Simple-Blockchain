import { sha256 } from '@noble/hashes/sha256';
import { ripemd160 } from '@noble/hashes/ripemd160';
import { bytesToHex, hexToBytes } from '@noble/hashes/utils';
import { etc, getPublicKey, sign, utils as secpUtils } from '@noble/secp256k1';
import bs58 from 'bs58';

const VERSION_BYTE = 0x00;

const SPKI_PREFIX = Uint8Array.from([
  0x30, 0x59,
  0x30, 0x13,
  0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01,
  0x06, 0x05, 0x2b, 0x81, 0x04, 0x00, 0x0a,
  0x03, 0x42, 0x00, 0x04,
]);

const textEncoder = new TextEncoder();

function concatBytes(a: Uint8Array, b: Uint8Array): Uint8Array {
  const result = new Uint8Array(a.length + b.length);
  result.set(a, 0);
  result.set(b, a.length);
  return result;
}

function toBase64(bytes: Uint8Array): string {
  if (typeof Buffer !== 'undefined') {
    return Buffer.from(bytes).toString('base64');
  }
  let binary = '';
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary);
}

function doubleSha256(data: Uint8Array): Uint8Array {
  return sha256(sha256(data));
}

export function randomPrivateKey(): Uint8Array {
  return secpUtils.randomPrivateKey();
}

export function privateKeyFromHex(hex: string): Uint8Array {
  return hexToBytes(hex);
}

export function privateKeyToHex(key: Uint8Array): string {
  return bytesToHex(key);
}

export function derivePublicKey(privKey: Uint8Array): Uint8Array {
  return getPublicKey(privKey, false);
}

export function publicKeyToSpki(uncompressed: Uint8Array): Uint8Array {
  if (uncompressed.length !== 65 || uncompressed[0] !== 0x04) {
    throw new Error('Public key must be uncompressed (65 bytes)');
  }
  const suffix = uncompressed.slice(1);
  return concatBytes(SPKI_PREFIX, suffix);
}

export function spkiToBase64(spki: Uint8Array): string {
  return toBase64(spki);
}

export function spkiToAddress(spki: Uint8Array): string {
  const sha = sha256(spki);
  const ripe = ripemd160(sha);

  const payload = new Uint8Array(1 + ripe.length);
  payload[0] = VERSION_BYTE;
  payload.set(ripe, 1);

  const checksum = doubleSha256(payload).slice(0, 4);
  const addressBytes = new Uint8Array(payload.length + checksum.length);
  addressBytes.set(payload, 0);
  addressBytes.set(checksum, payload.length);

  return bs58.encode(addressBytes);
}

function encodeDerInteger(bytes: Uint8Array): Uint8Array {
  let offset = 0;
  while (offset < bytes.length - 1 && bytes[offset] === 0) {
    offset++;
  }
  let value = bytes.slice(offset);
  if (value[0] & 0x80) {
    const extended = new Uint8Array(value.length + 1);
    extended[0] = 0;
    extended.set(value, 1);
    value = extended;
  }
  const result = new Uint8Array(2 + value.length);
  result[0] = 0x02;
  result[1] = value.length;
  result.set(value, 2);
  return result;
}

export function signMessage(privKey: Uint8Array, message: string): string {
  const digest = sha256(textEncoder.encode(message));
  const signature = sign(digest, privKey, { lowS: true });
  const rBytes = encodeDerInteger(etc.numberToBytesBE(signature.r));
  const sBytes = encodeDerInteger(etc.numberToBytesBE(signature.s));
  const der = new Uint8Array(2 + rBytes.length + sBytes.length);
  der[0] = 0x30;
  der[1] = rBytes.length + sBytes.length;
  der.set(rBytes, 2);
  der.set(sBytes, 2 + rBytes.length);
  return toBase64(der);
}
