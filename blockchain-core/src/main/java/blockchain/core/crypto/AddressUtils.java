package blockchain.core.crypto;

import java.security.PublicKey;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

/**
 * Tiny helper for Bitcoin-style Base-58 check addresses.
 *
 * Address = Base58( version(1) ‖ hash160(20) ‖ checksum(4) )
 */
public final class AddressUtils {

    /** Version byte 0 = main-net (fits demo). */
    private static final byte VERSION = 0x00;

    private AddressUtils() {}

    /* ------------------------------------------------------------------ */
    /* Encoding                                                            */
    /* ------------------------------------------------------------------ */

    public static String publicKeyToAddress(PublicKey pub) {
        byte[] sha256 = HashingUtils.computeSha256Bytes(pub.getEncoded());
        byte[] ripemd = ripemd160(sha256);

        return hash160ToAddress(ripemd);
    }

    /**
     * Encodes a raw RIPEMD-160 hash into a Base58Check address.
     *
     * <p>This mirrors the encoding performed in {@link #publicKeyToAddress(PublicKey)}
     * but allows callers that already operate on wallet hashes (e.g. when
     * interoperating with external tooling) to derive the textual address.
     *
     * @param hash160 20-byte RIPEMD-160 digest of the public key
     * @return Base58Check-encoded address
     */
    public static String hash160ToAddress(byte[] hash160) {
        if (hash160 == null || hash160.length != 20) {
            throw new IllegalArgumentException("hash160 must be exactly 20 bytes");
        }

        byte[] payload = new byte[1 + hash160.length + 4];
        payload[0] = VERSION;
        System.arraycopy(hash160, 0, payload, 1, hash160.length);

        byte[] checksum = checksum(payload, 0, 1 + hash160.length);
        System.arraycopy(checksum, 0, payload, 1 + hash160.length, 4);

        return Base58.encode(payload);
    }

    /* ------------------------------------------------------------------ */
    /* Validation                                                          */
    /* ------------------------------------------------------------------ */

    public static boolean isValid(String addr) {
        try {
            byte[] raw = Base58.decode(addr);
            if (raw.length != 25 || raw[0] != VERSION) return false;
            byte[] chk = checksum(raw, 0, 21);
            return Arrays.equals(chk, Arrays.copyOfRange(raw, 21, 25));
        } catch (IllegalArgumentException ex) { // bad Base-58
            return false;
        }
    }

    /* ------------------------------------------------------------------ */
    /* Internal helpers                                                    */
    /* ------------------------------------------------------------------ */

    private static byte[] ripemd160(byte[] data) {
        RIPEMD160Digest d = new RIPEMD160Digest();
        d.update(data, 0, data.length);
        byte[] out = new byte[20];
        d.doFinal(out, 0);
        return out;
    }

    private static byte[] checksum(byte[] src, int off, int len) {
        byte[] first  = HashingUtils.computeSha256Bytes(src, off, len);
        byte[] second = HashingUtils.computeSha256Bytes(first);
        return Arrays.copyOfRange(second, 0, 4);
    }
}
