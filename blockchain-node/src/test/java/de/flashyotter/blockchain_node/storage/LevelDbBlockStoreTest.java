package de.flashyotter.blockchain_node.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;

@SpringBootTest
class LevelDbBlockStoreTest {

    @Autowired
    private ObjectMapper mapper;       // ← will have your publicKeyModule() registered

    @Test
    void saveAndFindByHash(@TempDir Path tmp) {
        File dir = tmp.resolve("data").toFile();
        LevelDbBlockStore store = new LevelDbBlockStore(mapper, dir);

        Block genesis = new Chain().getLatest();
        store.save(genesis);

        Block loaded = store.findByHash(genesis.getHashHex());
        assertNotNull(loaded);
        assertEquals(genesis.getHashHex(), loaded.getHashHex());
    }

    @Test
    void missingReturnsNull() {
        LevelDbBlockStore store = new LevelDbBlockStore(mapper, new File("no-such-dir"));
        assertNull(store.findByHash("does-not-exist"));
    }

    @Test
    void persistsBlocksAcrossRestarts(@TempDir Path tmp) {
        File dir = tmp.resolve("data").toFile();

        LevelDbBlockStore store = new LevelDbBlockStore(mapper, dir);
        Block genesis = new Chain().getLatest();
        store.save(genesis);
        store.close();

        LevelDbBlockStore reopened = new LevelDbBlockStore(mapper, dir);
        Block loaded = reopened.findByHash(genesis.getHashHex());
        assertNotNull(loaded);
    }
}
