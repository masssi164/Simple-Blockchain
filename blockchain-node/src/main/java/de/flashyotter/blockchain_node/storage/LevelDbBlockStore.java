package de.flashyotter.blockchain_node.storage;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import jakarta.annotation.PreDestroy;

/**
 * Disk-backed block store using LevelDB.
 */
@Primary
public class LevelDbBlockStore implements BlockStore {

    private final ObjectMapper mapper;
    private final DB db;
    private static final int CACHE_LIMIT = 256;
    private final Map<String, Block> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(CACHE_LIMIT, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Block> eldest) {
                    return size() > CACHE_LIMIT;
                }
            }
    );

    /* DTOs for stable JSON serialization */
    private record HeaderDto(int height, String previousHashHex,
                             String merkleRootHex, int compactDifficultyBits,
                             long timeMillis, int nonce) {}
    private record BlockDto(HeaderDto header, java.util.List<Transaction> txList) {}

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
            it.seekToFirst();
            java.util.Map.Entry<byte[], byte[]> last = null;
            while (it.hasNext()) {
                last = it.next();
            }
            if (last != null) {
                BlockDto dto = mapper.readValue(last.getValue(), BlockDto.class);
                HeaderDto h = dto.header();
                Block tip = new Block(h.height(), h.previousHashHex(), dto.txList(),
                                      h.compactDifficultyBits(), h.timeMillis(), h.nonce());
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
            HeaderDto h = new HeaderDto(b.getHeight(), b.getPreviousHashHex(),
                    b.getMerkleRootHex(), b.getCompactDifficultyBits(),
                    b.getTimeMillis(), b.getNonce());
            BlockDto dto = new BlockDto(h, b.getTxList());
            db.put(
                b.getHashHex().getBytes(StandardCharsets.UTF_8),
                mapper.writeValueAsBytes(dto)
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
            if (bytes == null) return null;
            BlockDto dto = mapper.readValue(bytes, BlockDto.class);
            HeaderDto h = dto.header();
            Block b = new Block(h.height(), h.previousHashHex(), dto.txList(),
                                h.compactDifficultyBits(), h.timeMillis(), h.nonce());
            cache.put(hash, b);
            return b;
        } catch (IOException e) {
            throw new RuntimeException("LevelDB read failed", e);
        }
    }

    @Override
    public Iterable<Block> loadAll() {
        java.util.List<Block> list = new java.util.ArrayList<>();
        try (var it = db.iterator()) {
            for (it.seekToFirst(); it.hasNext(); ) {
                var entry = it.next();
                BlockDto dto = mapper.readValue(entry.getValue(), BlockDto.class);
                HeaderDto h = dto.header();
                list.add(new Block(h.height(), h.previousHashHex(), dto.txList(),
                                   h.compactDifficultyBits(), h.timeMillis(), h.nonce()));
            }
        } catch (IOException e) {
            throw new RuntimeException("LevelDB iteration failed", e);
        }
        return list;
    }

    @PreDestroy
    public void close() {
        try {
            db.close();
        } catch (IOException ignored) { }
    }
}
