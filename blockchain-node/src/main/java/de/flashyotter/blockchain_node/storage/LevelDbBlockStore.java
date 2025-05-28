package de.flashyotter.blockchain_node.storage;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Block;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Disk-backed block store using LevelDB.
 */
@Primary
public class LevelDbBlockStore implements BlockStore {

    private final ObjectMapper mapper;
    private final DB db;
    private final Map<String, Block> cache = new ConcurrentHashMap<>();

    /** Standard-Konstruktor, nutzt festen Pfad "data/blocks" */
    public LevelDbBlockStore(ObjectMapper mapper) {
        this(mapper, new File("data/blocks"));
    }

    /** Neuer Konstruktor für Tests oder alternative Pfade */
    public LevelDbBlockStore(ObjectMapper mapper, File blocksDir) {
        this.mapper = mapper;
        try {
            this.db = factory.open(blocksDir, new Options().createIfMissing(true));
        } catch (IOException e) {
            throw new RuntimeException("Unable to open LevelDB at " + blocksDir, e);
        }
    }

    @Override
    public void save(Block b) {
        cache.put(b.getHashHex(), b);
        try {
            db.put(b.getHashHex().getBytes(StandardCharsets.UTF_8),
                   mapper.writeValueAsBytes(b));
        } catch (IOException e) {
            throw new RuntimeException("LevelDB save failed", e);
        }
    }

    @Override
    public Block findByHash(String hash) {
        Block fromCache = cache.get(hash);
        if (fromCache != null) {
            return fromCache;
        }
        try {
            
            byte[] bytes = db.get(hash.getBytes(StandardCharsets.UTF_8));
            return (bytes == null) ? null : mapper.readValue(bytes, Block.class);
        } catch (IOException e) {
            throw new RuntimeException("LevelDB read failed", e);
        }
    }

    @PreDestroy
    public void close() {
        try { db.close(); } catch (IOException ignored) { }
    }
}
