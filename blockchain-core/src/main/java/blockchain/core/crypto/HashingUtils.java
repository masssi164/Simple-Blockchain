package blockchain.core.crypto;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe crypto primitives + BTC “bits” helpers.
 * Guava handles SHA-256 & hex → ~80 LOC removed vs. manual implementation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashingUtils {


    public static byte[] computeSha256Bytes(String data) {
        return Hashing.sha256()
                      .hashString(data, StandardCharsets.UTF_8)
                      .asBytes();
    }

    public static String computeSha256Hex(String data) {
        return Hashing.sha256()
                      .hashString(data, StandardCharsets.UTF_8)
                      .toString();           // already hex-lowercase
    }


    public static String computeMerkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) return computeSha256Hex("");
        List<String> layer = new ArrayList<>(hashes);

        // BTC rule: if odd, duplicate last
        while (layer.size() > 1) {
            if ((layer.size() & 1) == 1)
                layer.add(layer.get(layer.size() - 1));

            for (int i = 0; i < layer.size(); i += 2)
                layer.set(i / 2, computeSha256Hex(layer.get(i) + layer.get(i + 1)));

            layer = layer.subList(0, layer.size() / 2);
        }
        return layer.get(0);
    }


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


    public static String bytesToHex(byte[] bytes) {
        return BaseEncoding.base16().lowerCase().encode(bytes);
    }
}
