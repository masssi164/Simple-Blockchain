package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

class ChainEdgeCaseTest {

    @Test
    void addBlockEmptyTxListThrows() {
        Chain chain = new Chain();
        String prevHash = chain.getLatest().getHashHex();
        int bits = chain.getLatest().getCompactDifficultyBits();

        Block emptyBlock = new Block(1, prevHash, List.of(), bits);
        // empty block → "empty block"
        BlockchainException ex = assertThrows(BlockchainException.class,
            () -> chain.addBlock(emptyBlock));
        assertEquals("empty block", ex.getMessage());
    }

    @Test
    void addBlockWithNonCoinbaseFirstTransactionThrows() {
        Chain chain = new Chain();
        String prevHash = chain.getLatest().getHashHex();
        int bits = chain.getLatest().getCompactDifficultyBits();

        // set up a "normal" Tx (nicht coinbase)
        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();
        String utxoId = "utxo:0";
        TxOutput utxo = new TxOutput(10.0, alice.getPublicKey());
        // craft und signiere die Transaktion
        Transaction tx = new Transaction();
        tx.getInputs().add(new TxInput(utxoId, new byte[0], alice.getPublicKey()));
        tx.getOutputs().add(new TxOutput(10.0, bob.getPublicKey()));
        tx.signInputs(alice.getPrivateKey());

        Block badBlock = new Block(1, prevHash, List.of(tx), bits);
        BlockchainException ex = assertThrows(BlockchainException.class,
            () -> chain.addBlock(badBlock));
        assertEquals("first tx must be coinbase", ex.getMessage());
    }

    @Test
    void addBlockWithWrongPrevHashThrows() {
        Chain chain = new Chain();
        // coinbase für Höhe 1
        Wallet miner = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(1));
        int bits = chain.getLatest().getCompactDifficultyBits();

        Block candidate = new Block(1, "deadbeef".repeat(8),
                                    List.of(cb), bits);
        candidate.mineLocally();

        BlockchainException ex = assertThrows(BlockchainException.class,
            () -> chain.addBlock(candidate));
        assertEquals("prev-hash mismatch", ex.getMessage());
    }
}
