package blockchain.core.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

/**
 * Thread-safe crypto primitives + BTC “bits” helpers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashingUtils {

    private static final ThreadLocal<MessageDigest> SHA256 =
            ThreadLocal.withInitial(() -> {
                try { 
                    return MessageDigest.getInstance("SHA-256"); 
                } catch (Exception e) { throw new IllegalStateException(e); }
            });

    /* ---- SHA-256 ---- */

    public static byte[] computeSha256Bytes(String data) {
        MessageDigest d = SHA256.get(); d.reset();
        return d.digest(data.getBytes());
    }

    public static String computeSha256Hex(String data) {
        return bytesToHex(computeSha256Bytes(data));
    }

    /* ---- Merkle root ---- */
    public static String computeMerkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) return computeSha256Hex("");
        while (hashes.size() > 1) {
            if ((hashes.size() & 1) == 1)
                hashes.add(hashes.get(hashes.size() - 1));           // BTC rule
            for (int i = 0; i < hashes.size(); i += 2)
                hashes.set(i / 2, computeSha256Hex(hashes.get(i) + hashes.get(i + 1)));
            hashes = hashes.subList(0, hashes.size() / 2);
        }
        return hashes.get(0);
    }

    /* ---- “bits” ⇄ target ---- */

    public static BigInteger compactToTarget(int bits) {
        int exp = bits >>> 24;
        int mant = bits & 0x007fffff;
        BigInteger bn = BigInteger.valueOf(mant);
        return (exp <= 3) ? bn.shiftRight(8 * (3 - exp)) : bn.shiftLeft(8 * (exp - 3));
    }

    public static int targetToCompact(BigInteger target) {
        if (target.signum() <= 0) return 0;
        int size = (target.bitLength() + 7) / 8;
        int cmp  = (size <= 3)
                ? target.intValue() << 8 * (3 - size)
                : target.shiftRight(8 * (size - 3)).intValue();
        if ((cmp & 0x0080_0000) != 0) { cmp >>= 8; size++; }
        return (size << 24) | (cmp & 0x007fffff);
    }

    /* ---- helpers ---- */

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String h = Integer.toHexString(b & 0xff);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return sb.toString();
    }
}
