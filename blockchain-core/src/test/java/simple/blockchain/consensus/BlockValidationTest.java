package simple.blockchain.consensus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                                               ConsensusParams.blockReward(1),
                                               "1");
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
                                               ConsensusParams.blockReward(1) * 2,
                                               "1");
        Block b = new Block(1, prev.getHashHex(), List.of(coinbase), prev.getCompactDifficultyBits());
        b.mineLocally();

        BlockchainException ex = assertThrows(BlockchainException.class, () -> chain.addBlock(b));
        assertEquals("excessive coinbase", ex.getMessage());
    }

    @Test
    @DisplayName("Outputs from earlier tx in the same block can be spent")
    void spendOutputsWithinBlock() {
        Chain chain = new Chain();
        Block prev  = chain.getLatest();

        Wallet miner = new Wallet();
        Transaction firstCb = new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(1), "1");
        Block first = new Block(1, prev.getHashHex(), List.of(firstCb), prev.getCompactDifficultyBits());
        first.mineLocally();
        chain.addBlock(first);

        String matureId = firstCb.getOutputs().get(0).id(firstCb.calcHashHex(), 0);

        // advance chain to mature the coinbase
        Block last = first;
        for (int h = 2; h <= ConsensusParams.COINBASE_MATURITY; h++) {
            Transaction cb = new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(h), String.valueOf(h));
            Block filler = new Block(h, last.getHashHex(), List.of(cb), last.getCompactDifficultyBits());
            filler.mineLocally();
            chain.addBlock(filler);
            last = filler;
        }

        Wallet bob   = new Wallet();
        Wallet charlie = new Wallet();

        Transaction fund = new Transaction();
        fund.getInputs().add(new TxInput(matureId, new byte[0], miner.getPublicKey()));
        fund.getOutputs().add(new TxOutput(ConsensusParams.blockReward(1), bob.getPublicKey()));
        fund.signInputs(miner.getPrivateKey());
        String fundOut = fund.getOutputs().get(0).id(fund.calcHashHex(), 0);

        Transaction spend = new Transaction();
        spend.getInputs().add(new TxInput(fundOut, new byte[0], bob.getPublicKey()));
        spend.getOutputs().add(new TxOutput(ConsensusParams.blockReward(1), charlie.getPublicKey()));
        spend.signInputs(bob.getPrivateKey());

        Block b = new Block(last.getHeight() + 1, last.getHashHex(), List.of(new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(last.getHeight() + 1), "x"), fund, spend), last.getCompactDifficultyBits());
        b.mineLocally();

        chain.addBlock(b);

        String spendId = spend.getOutputs().get(0).id(spend.calcHashHex(), 0);
        assertTrue(chain.getUtxoSnapshot().containsKey(spendId));
    }

    @Test
    @DisplayName("Coinbase outputs require maturity before spending")
    void rejectImmatureCoinbaseSpend() {
        Chain chain = new Chain();
        Block prev  = chain.getLatest();

        Wallet miner = new Wallet();
        Transaction coinbase = new Transaction(miner.getPublicKey(),
                                               ConsensusParams.blockReward(1),
                                               "1");
        String cbOut = coinbase.getOutputs().get(0)
                               .id(coinbase.calcHashHex(), 0);

        Transaction spend = new Transaction();
        spend.getInputs().add(new TxInput(cbOut, new byte[0], miner.getPublicKey()));
        spend.getOutputs().add(new TxOutput(ConsensusParams.blockReward(1), miner.getPublicKey()));
        spend.signInputs(miner.getPrivateKey());

        Block b = new Block(1, prev.getHashHex(), List.of(coinbase, spend), prev.getCompactDifficultyBits());
        b.mineLocally();

        BlockchainException ex = assertThrows(BlockchainException.class, () -> chain.addBlock(b));
        assertEquals("coinbase immature", ex.getMessage());
    }

    @Test
    @DisplayName("Coinbase outputs may be spent after maturity")
    void spendMatureCoinbase() {
        Chain chain = new Chain();
        Block prev  = chain.getLatest();

        Wallet miner = new Wallet();
        Transaction coinbase = new Transaction(miner.getPublicKey(),
                                               ConsensusParams.blockReward(1),
                                               "1");
        Block first = new Block(1, prev.getHashHex(), List.of(coinbase), prev.getCompactDifficultyBits());
        first.mineLocally();
        chain.addBlock(first);

        String cbOut = coinbase.getOutputs().get(0)
                               .id(coinbase.calcHashHex(), 0);

        // Mine maturity blocks with empty coinbase transactions
        Block prevBlock = first;
        for (int h = 2; h <= ConsensusParams.COINBASE_MATURITY; h++) {
            Transaction cb = new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(h), String.valueOf(h));
            Block filler = new Block(h, prevBlock.getHashHex(), List.of(cb), prevBlock.getCompactDifficultyBits());
            filler.mineLocally();
            chain.addBlock(filler);
            prevBlock = filler;
        }

        Transaction spend = new Transaction();
        spend.getInputs().add(new TxInput(cbOut, new byte[0], miner.getPublicKey()));
        spend.getOutputs().add(new TxOutput(ConsensusParams.blockReward(1), miner.getPublicKey()));
        spend.signInputs(miner.getPrivateKey());

        Block mature = new Block(prevBlock.getHeight() + 1, prevBlock.getHashHex(), List.of(new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(prevBlock.getHeight() + 1), "m"), spend), prevBlock.getCompactDifficultyBits());
        mature.mineLocally();

        chain.addBlock(mature);

        String spendId = spend.getOutputs().get(0).id(spend.calcHashHex(), 0);
        assertTrue(chain.getUtxoSnapshot().containsKey(spendId));
    }
}
