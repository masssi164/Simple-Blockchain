package de.flashyotter.blockchain_node.storage;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.config.JacksonConfig;
import de.flashyotter.blockchain_node.config.NodeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelDbBlockStoreTest {

    @BeforeAll
    static void initMapper() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.registerModule(new JacksonConfig().publicKeyModule());
        JsonUtils.use(mapper);
    }

    @TempDir
    Path temp;

    @Test
    void blockPersistsAcrossRestart() throws Exception {
        NodeProperties props = new NodeProperties();
        props.setDataPath(temp.toString());

        Chain chain = new Chain();
        Block genesis = chain.getLatest();

        LevelDbBlockStore store = new LevelDbBlockStore(props);
        store.save(genesis);
        store.close();

        LevelDbBlockStore reopened = new LevelDbBlockStore(props);
        Block loaded = reopened.findByHash(genesis.getHashHex());
        reopened.close();

        assertNotNull(loaded);
        assertEquals(genesis.getHashHex(), loaded.getHashHex());
    }

    @Test
    void loadAllAfterRestartReturnsBlocks() throws Exception {
        NodeProperties props = new NodeProperties();
        props.setDataPath(temp.toString());

        Chain chain = new Chain();
        Block genesis = chain.getLatest();

        LevelDbBlockStore first = new LevelDbBlockStore(props);
        first.save(genesis);
        first.close();

        LevelDbBlockStore second = new LevelDbBlockStore(props);
        List<Block> blocks = new ArrayList<>();
        for (Block b : second.loadAll()) {
            blocks.add(b);
        }
        second.close();

        assertEquals(1, blocks.size());
        assertEquals(genesis.getHashHex(), blocks.get(0).getHashHex());
    }
}
