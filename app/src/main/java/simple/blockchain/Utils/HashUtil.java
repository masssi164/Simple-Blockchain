package simple.blockchain.Utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public final class HashUtil {

    private HashUtil() {}

    /* ---------- SHA‑256 ---------- */

    public static byte[] sha256Bytes(String input) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            return d.digest(input.getBytes("UTF-8"));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public static String sha256(String input) { return bytes2Hex(sha256Bytes(input)); }
    public static String bytes2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return sb.toString();
    }

    /* ---------- Merkle‑Root ---------- */

    public static String merkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) return sha256("");
        while (hashes.size() > 1) {
            if (hashes.size() % 2 != 0) hashes.add(hashes.get(hashes.size() - 1));
            for (int i = 0; i < hashes.size(); i += 2) {
                hashes.set(i / 2, sha256(hashes.get(i) + hashes.get(i + 1)));
            }
            hashes = hashes.subList(0, hashes.size() / 2);
        }
        return hashes.get(0);
    }

    /* ---------- Bitcoin “bits” helper ---------- */

    public static BigInteger compactToBigInt(int bits) {
        int exp = bits >>> 24;
        int mant = bits & 0x007fffff;
        BigInteger bn = BigInteger.valueOf(mant);
        return exp <= 3 ? bn.shiftRight(8 * (3 - exp)) : bn.shiftLeft(8 * (exp - 3));
    }
    public static int bigIntToCompact(BigInteger bn) {
        if (bn.signum() <= 0) return 0;
        int size = (bn.bitLength() + 7) / 8;
        int compact = size <= 3 ? bn.intValue() << 8 * (3 - size)
                                : bn.shiftRight(8 * (size - 3)).intValue();
        if ((compact & 0x0080_0000) != 0) { compact >>= 8; size++; }
        return (size << 24) | (compact & 0x007f_ffff);
    }
}
