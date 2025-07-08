package de.flashyotter.blockchain_node.storage;

import blockchain.core.consensus.Chain;
import blockchain.core.consensus.ConsensusParams;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.config.JacksonConfig;
import de.flashyotter.blockchain_node.config.NodeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WriteAheadLogBlockStoreTest {

    @BeforeAll
    static void initMapper() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.registerModule(new JacksonConfig().publicKeyModule());
        JsonUtils.use(mapper);
    }

    @TempDir
    Path temp;

    @Test
    void recoverFromLogWhenDbLost() throws Exception {
        NodeProperties props = new NodeProperties();
        props.setDataPath(temp.toString());

        WriteAheadLogBlockStore store = new WriteAheadLogBlockStore(new LevelDbBlockStore(props), props);
        Chain c = new Chain();
        Block g = c.getLatest();

        Wallet miner = new Wallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(1), "1");
        Block b1 = new Block(1, g.getHashHex(), List.of(cb), g.getCompactDifficultyBits());
        b1.mineLocally();
        c.addBlock(b1);
        store.save(b1);
        store.close();

        // simulate lost LevelDB
        Path dbDir = temp.resolve("blocks");
        if (Files.exists(dbDir)) {
            Files.walk(dbDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } catch (Exception ignored) {}
                    });
        }

        WriteAheadLogBlockStore reopened = new WriteAheadLogBlockStore(new LevelDbBlockStore(props), props);
        List<Block> blocks = new ArrayList<>();
        for (Block b : reopened.loadAll()) blocks.add(b);

        Chain rebuilt = new Chain();
        blocks.stream()
              .sorted(Comparator.comparingInt(Block::getHeight))
              .filter(bl -> bl.getHeight() > 0)
              .forEach(rebuilt::addBlock);

        assertEquals(2, rebuilt.getBlocks().size());
        assertEquals(b1.getHashHex(), rebuilt.getLatest().getHashHex());
        reopened.close();
    }
}
