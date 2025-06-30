package de.flashyotter.blockchain_node.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.storage.InMemoryBlockStore;
import de.flashyotter.blockchain_node.storage.BlockStore;

class ChainBootstrapTest {

    private ObjectMapper mapper;

    @BeforeEach
    void initMapper() {
        mapper = new ObjectMapper().findAndRegisterModules();
        mapper.registerModule(new JacksonConfig().publicKeyModule());
        JsonUtils.use(mapper);
    }

    private Block mineBlock(int height, String prevHash, int bits, Wallet miner) {
        Transaction cb = new Transaction(miner.getPublicKey(), ConsensusParams.blockReward(height));
        Block b = new Block(height, prevHash, List.of(cb), bits);
        b.mineLocally();
        return b;
    }

    private Chain build(BlockStore store) {
        return new CoreConsensusConfig().chain(store);
    }

    @Test
    void emptyStoreLoadsGenesisOnly() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        Chain chain = build(store);
        assertEquals(1, chain.getBlocks().size());
        assertEquals(0, chain.getLatest().getHeight());
    }

    @Test
    void singleBlockRestored() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        Chain tmp = new Chain();
        Wallet miner = new Wallet();
        Block b1 = mineBlock(1, tmp.getLatest().getHashHex(), tmp.getLatest().getCompactDifficultyBits(), miner);
        store.save(b1);
        Chain chain = build(store);
        assertEquals(1, chain.getLatest().getHeight());
    }

    @Test
    void blocksRestoredInOrder() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        Chain tmp = new Chain();
        Wallet miner = new Wallet();
        Block b1 = mineBlock(1, tmp.getLatest().getHashHex(), tmp.getLatest().getCompactDifficultyBits(), miner);
        tmp.addBlock(b1);
        Block b2 = mineBlock(2, b1.getHashHex(), b1.getCompactDifficultyBits(), miner);
        tmp.addBlock(b2);
        Block b3 = mineBlock(3, b2.getHashHex(), b2.getCompactDifficultyBits(), miner);
        tmp.addBlock(b3);
        store.save(b2);
        store.save(b1);
        store.save(b3);
        Chain chain = build(store);
        assertEquals(3, chain.getLatest().getHeight());
        assertEquals(List.of(0,1,2,3), chain.getBlocks().stream().map(Block::getHeight).toList());
    }

    @Test
    void heavierForkChosen() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        Wallet miner = new Wallet();
        Chain tmp = new Chain();
        Block h1 = mineBlock(1, tmp.getLatest().getHashHex(), tmp.getLatest().getCompactDifficultyBits(), miner);
        tmp.addBlock(h1);
        Block h2 = mineBlock(2, h1.getHashHex(), h1.getCompactDifficultyBits(), miner);
        tmp.addBlock(h2);
        Block h3 = mineBlock(3, h2.getHashHex(), h2.getCompactDifficultyBits(), miner);
        tmp.addBlock(h3);
        Block h4 = mineBlock(4, h3.getHashHex(), h3.getCompactDifficultyBits(), miner);
        tmp.addBlock(h4);
        Block heavy5 = mineBlock(5, h4.getHashHex(), h4.getCompactDifficultyBits(), miner);
        tmp.addBlock(heavy5);
        Block heavy6 = mineBlock(6, heavy5.getHashHex(), heavy5.getCompactDifficultyBits(), miner);
        tmp.addBlock(heavy6);
        store.save(h1); store.save(h2); store.save(h3); store.save(h4); store.save(heavy5); store.save(heavy6);
        Block alt5 = mineBlock(5, h4.getHashHex(), h4.getCompactDifficultyBits(), miner);
        store.save(alt5);
        Chain chain = build(store);
        assertEquals(6, chain.getLatest().getHeight());
    }

    @Test
    void invalidBlockSkipped() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        Chain tmp = new Chain();
        Wallet miner = new Wallet();
        Block b1 = mineBlock(1, tmp.getLatest().getHashHex(), tmp.getLatest().getCompactDifficultyBits(), miner);
        store.save(b1);
        Block bogus = mineBlock(2, "deadbeef".repeat(8), b1.getCompactDifficultyBits(), miner);
        store.save(bogus);
        Chain chain = build(store);
        assertEquals(1, chain.getLatest().getHeight());
    }
}
