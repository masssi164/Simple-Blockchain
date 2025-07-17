package simple.blockchain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import blockchain.core.serialization.JsonUtils;

/** Verify Block JSON round-trips using the public constructors. */
class BlockJsonTest {
    @Test
    void blockSerializesAndDeserializes() {
        Wallet w = new Wallet();
        Transaction coinbase = new Transaction(w.getPublicKey(), 5.0, "0");
        Block b = new Block(1, "0", List.of(coinbase), 0x1f0fffff, 0L, 0);

        String json = JsonUtils.toJson(b);
        Block parsed = JsonUtils.blockFromJson(json);

        assertEquals(b.getHeight(), parsed.getHeight());
        assertEquals(b.getPreviousHashHex(), parsed.getPreviousHashHex());
        assertEquals(b.getMerkleRootHex(), parsed.getMerkleRootHex());
        assertEquals(b.getNonce(), parsed.getNonce());
    }
}
