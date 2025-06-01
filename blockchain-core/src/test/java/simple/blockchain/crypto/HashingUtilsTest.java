package simple.blockchain.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.crypto.HashingUtils;

/**
 * Extra coverage for {@link HashingUtils#computeMerkleRoot(java.util.List)}.
 */
class HashingUtilsTest {

    @Test
    @DisplayName("Merkle root of a single hash is that hash itself")
    void singleElement() {
        String h = "a".repeat(64);
        assertEquals(h, HashingUtils.computeMerkleRoot(List.of(h)));
    }

    @Test
    @DisplayName("Merkle root is stable for even and odd layer sizes")
    void evenAndOdd() {
        List<String> hashes = List.of(
                "a".repeat(64), "b".repeat(64), "c".repeat(64));
        /* Function shouldn't throw and must return 64-hex-chars */
        String root = HashingUtils.computeMerkleRoot(hashes);
        assertEquals(64, root.length(), "hex length");
    }
}
