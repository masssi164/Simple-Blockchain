package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.crypto.AddressUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

class ChainEdgeCaseTest {

    @Test
    void addBlockEmptyTxListThrows() {
        Chain chain   = new Chain();
        String prev   = chain.getLatest().getHashHex();
        int bits      = chain.getLatest().getCompactDifficultyBits();

        Block empty = new Block(1, prev, List.of(), bits);

        BlockchainException ex = assertThrows(BlockchainException.class,
                () -> chain.addBlock(empty));
        assertEquals("empty block", ex.getMessage());
    }

    @Test
    void addBlockWithNonCoinbaseFirstTransactionThrows() {
        Chain   chain   = new Chain();
        String  prev    = chain.getLatest().getHashHex();
        int     bits    = chain.getLatest().getCompactDifficultyBits();

        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();

        /* ──  eine ganz normale, signierte Zahlung  ───────────────────── */
        String   utxoId = "utxo:0";
        TxOutput utxo   = new TxOutput(10.0,
                AddressUtils.publicKeyToAddress(alice.getPublicKey()));   // dummy-UTXO

        Transaction tx = new Transaction();
        tx.getInputs().add(new TxInput(utxoId, new byte[0], alice.getPublicKey()));
        tx.getOutputs().add(new TxOutput(10.0,
                AddressUtils.publicKeyToAddress(bob.getPublicKey())));    // **fehlte**
        tx.signInputs(alice.getPrivateKey());

        Block bad = new Block(1, prev, List.of(tx), bits);

        BlockchainException ex = assertThrows(BlockchainException.class,
                () -> chain.addBlock(bad));
        assertEquals("first tx must be coinbase", ex.getMessage());
    }

    @Test
    void addBlockWithWrongPrevHashThrows() {
        Chain  chain = new Chain();
        Wallet miner = new Wallet();

        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(1),
                                         "1");
        int bits = chain.getLatest().getCompactDifficultyBits();

        Block candidate = new Block(1, "deadbeef".repeat(8), List.of(cb), bits);
        candidate.mineLocally();

        BlockchainException ex = assertThrows(BlockchainException.class,
                () -> chain.addBlock(candidate));
        assertEquals("prev-hash mismatch", ex.getMessage());
    }

    @Test
    @DisplayName("Heavier fork re-organises the active chain")
    void forkReorganisation() {
        Chain c = new Chain();
        Block g = c.getLatest();

        Wallet miner = new Wallet();

        /* ---------- Branch A (2 Blöcke) ------------------------------ */
        Transaction cb1 = new Transaction(miner.getPublicKey(),
                                          ConsensusParams.blockReward(1),
                                          "1");
        Block a1 = new Block(1, g.getHashHex(), List.of(cb1),
                             g.getCompactDifficultyBits());
        a1.mineLocally();
        c.addBlock(a1);

        /* ---------- konkurrierende Branch B (3 Blöcke, mehr Arbeit) -- */
        Transaction cb2 = new Transaction(miner.getPublicKey(),
                                          ConsensusParams.blockReward(1),
                                          "1");
        Block b1 = new Block(1, g.getHashHex(), List.of(cb2),
                             g.getCompactDifficultyBits());
        b1.mineLocally();
        c.addBlock(b1);

        Transaction cb3 = new Transaction(miner.getPublicKey(),
                                          ConsensusParams.blockReward(2),
                                          "2");
        Block b2 = new Block(2, b1.getHashHex(), List.of(cb3),
                             g.getCompactDifficultyBits());
        b2.mineLocally();
        c.addBlock(b2);                         // ➜ Re-Org

        assertEquals(b2, c.getLatest(), "tip switched to heavier fork");
        assertEquals(3, c.getBlocks().size(), "active height now 3 (g,b1,b2)");
    }
}
