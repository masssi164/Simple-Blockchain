package de.flashyotter.blockchain_node.storage;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Block;
import jakarta.annotation.PreDestroy;

/**
 * Disk-backed block store using LevelDB.
 */
@Primary
public class LevelDbBlockStore implements BlockStore {

    private final ObjectMapper mapper;
    private final DB db;
    private final Map<String, Block> cache = new ConcurrentHashMap<>();

    /** Standard constructor, uses fixed path "data/blocks" */
    public LevelDbBlockStore(ObjectMapper mapper) {
        this(mapper, new File("data/blocks"));
    }

    /** 
     * Constructor for tests / alternative paths. 
     * Must assign both mapper and db before using them.
     */
    public LevelDbBlockStore(ObjectMapper mapper, File blocksDir) {
        this.mapper = mapper;
        try {
            // Open (or create) a LevelDB instance at the given directory
            Options opts = new Options();
            opts.createIfMissing(true);
            this.db = factory.open(blocksDir, opts);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open LevelDB at " + blocksDir, e);
        }

        // lazy pre-load tip for fast bootstrapping of the Chain
        try (var it = db.iterator()) {
            it.seekToLast();
            if (it.hasNext()) {
                var entry = it.next();
                Block tip = mapper.readValue(entry.getValue(), Block.class);
                cache.put(tip.getHashHex(), tip);
            }
        } catch (IOException ignore) { 
            // ignoring IO exceptions on pre-load 
        }
    }

    @Override
    public void save(Block b) {
        cache.put(b.getHashHex(), b);
        try {
            db.put(
                b.getHashHex().getBytes(StandardCharsets.UTF_8),
                mapper.writeValueAsBytes(b)
            );
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
        try {
            db.close();
        } catch (IOException ignored) { }
    }
}
