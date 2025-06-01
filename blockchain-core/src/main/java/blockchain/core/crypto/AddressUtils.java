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

        byte[] payload = new byte[1 + ripemd.length + 4];
        payload[0] = VERSION;
        System.arraycopy(ripemd, 0, payload, 1, ripemd.length);

        byte[] checksum = checksum(payload, 0, 1 + ripemd.length);
        System.arraycopy(checksum, 0, payload, 1 + ripemd.length, 4);

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
