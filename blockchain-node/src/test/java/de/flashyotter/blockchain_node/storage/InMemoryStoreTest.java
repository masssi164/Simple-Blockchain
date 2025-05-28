package de.flashyotter.blockchain_node.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;

class InMemoryBlockStoreTest {

    @Test
    void saveAndFindByHash() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        Chain chain = new Chain();
        Block genesis = chain.getLatest();

        // speichern
        store.save(genesis);
        // finden
        Block loaded = store.findByHash(genesis.getHashHex());
        assertNotNull(loaded);
        assertEquals(genesis.getHashHex(), loaded.getHashHex());
    }

    @Test
    void findByHashMissingReturnsNull() {
        InMemoryBlockStore store = new InMemoryBlockStore();
        assertNull(store.findByHash("does-not-exist"));
    }
}
