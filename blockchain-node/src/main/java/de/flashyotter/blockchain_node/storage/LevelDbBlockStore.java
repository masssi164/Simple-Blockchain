package de.flashyotter.blockchain_node.storage;

import blockchain.core.model.Block;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 * Disk-backed block store using LevelDB.
 * Keys   → blockHash (UTF-8)  
 * Values → raw JSON of {@link Block}
 */
@Primary
public class LevelDbBlockStore implements BlockStore {

    private final ObjectMapper mapper;
    private final DB db;

    public LevelDbBlockStore(ObjectMapper mapper) {
        this.mapper = mapper;
        try {
            this.db = factory.open(
                    new File("data/blocks"), new Options().createIfMissing(true));
        } catch (IOException e) {
            throw new RuntimeException("Unable to open LevelDB", e);
        }
    }

    @Override
    public void save(Block b) {
        try {
            db.put(b.getHashHex().getBytes(StandardCharsets.UTF_8),
                   mapper.writeValueAsBytes(b));
        } catch (IOException e) {
            throw new RuntimeException("LevelDB save failed", e);
        }
    }

    @Override
    public Block findByHash(String hash) {
        try {
            byte[] bytes = db.get(hash.getBytes(StandardCharsets.UTF_8));
            return (bytes == null) ? null : mapper.readValue(bytes, Block.class);
        } catch (IOException e) {
            throw new RuntimeException("LevelDB read failed", e);
        }
    }

    /** Close DB gracefully on shutdown. */
    @PreDestroy
    public void close() {
        try { db.close(); } catch (IOException ignored) { }
    }
}
