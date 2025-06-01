package blockchain.core.crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Thread-safe crypto primitives + BTC “bits” helpers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashingUtils {

    

    /* ───────────────────── BTC “compact bits” helpers ───────────────────── */

    public static BigInteger compactToTarget(int bits) {
        int exp  = bits >>> 24;
        int mant = bits & 0x007fffff;
        BigInteger bn = BigInteger.valueOf(mant);
        return (exp <= 3)
                ? bn.shiftRight(8 * (3 - exp))
                : bn.shiftLeft (8 * (exp - 3));
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

    /* ───────────────────── misc ───────────────────── */

    public static String bytesToHex(byte[] bytes) {
        return BaseEncoding.base16().lowerCase().encode(bytes);
    }

        /* ─────────────────── SHA-256 helpers ─────────────────── */

    public static byte[] computeSha256Bytes(String data) {
        return Hashing.sha256()
                      .hashString(data, StandardCharsets.UTF_8)
                      .asBytes();
    }

    /** Hash arbitrary binary payload. */
    public static byte[] computeSha256Bytes(byte[] data) {
        return Hashing.sha256().hashBytes(data).asBytes();
    }

    /** Hash a slice of a byte-array. */
    public static byte[] computeSha256Bytes(byte[] data, int off, int len) {
        return Hashing.sha256()
                      .hashBytes(java.util.Arrays.copyOfRange(data, off, off + len))
                      .asBytes();
    }

    public static String computeSha256Hex(String data) {
        return Hashing.sha256()
                      .hashString(data, StandardCharsets.UTF_8)
                      .toString();                    // already hex-lowercase
    }

    /* ─────────────────── Merkle & compact helpers ─────────────────── */

    public static String computeMerkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) return computeSha256Hex("");
        List<String> layer = new ArrayList<>(hashes);

        while (layer.size() > 1) {
            /* duplicate last element for odd-sized layers */
            if ((layer.size() & 1) == 1)
            layer.add(layer.get(layer.size() - 1));
            for (int i = 0; i < layer.size(); i += 2) {
                layer.set(i / 2,
                          computeSha256Hex(layer.get(i) + layer.get(i + 1)));
            }
            /* keep only the newly built half */
            layer = layer.subList(0, layer.size() / 2);
        }
        return layer.get(0);
    }

}
