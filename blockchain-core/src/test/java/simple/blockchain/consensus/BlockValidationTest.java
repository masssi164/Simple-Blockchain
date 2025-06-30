package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/** Tests for block validation rules enforced by Chain. */
class BlockValidationTest {

    @Test
    @DisplayName("Block with unknown UTXO is rejected")
    void rejectMissingUtxo() {
        Chain chain = new Chain();
        Block prev  = chain.getLatest();
        Wallet miner = new Wallet();

        Transaction coinbase = new Transaction(miner.getPublicKey(),
                                               ConsensusParams.blockReward(1));
        Transaction tx = new Transaction();
        tx.getInputs().add(new TxInput("doesnt:exist", new byte[0], miner.getPublicKey()));
        tx.getOutputs().add(new TxOutput(1.0, miner.getPublicKey()));
        tx.signInputs(miner.getPrivateKey());

        Block b = new Block(1, prev.getHashHex(), List.of(coinbase, tx), prev.getCompactDifficultyBits());
        b.mineLocally();

        BlockchainException ex = assertThrows(BlockchainException.class, () -> chain.addBlock(b));
        assertEquals("UTXO not found", ex.getMessage());
    }

    @Test
    @DisplayName("Coinbase reward exceeding limit is rejected")
    void rejectExcessiveCoinbase() {
        Chain chain = new Chain();
        Block prev  = chain.getLatest();
        Wallet miner = new Wallet();

        Transaction coinbase = new Transaction(miner.getPublicKey(),
                                               ConsensusParams.blockReward(1) * 2);
        Block b = new Block(1, prev.getHashHex(), List.of(coinbase), prev.getCompactDifficultyBits());
        b.mineLocally();

        BlockchainException ex = assertThrows(BlockchainException.class, () -> chain.addBlock(b));
        assertEquals("excessive coinbase", ex.getMessage());
    }
}
