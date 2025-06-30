package de.flashyotter.blockchain_node.storage;

import blockchain.core.model.Block;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/** In-memory store (non-persistent). */
@Component
public class InMemoryBlockStore implements BlockStore {

    private final Map<String, Block> db = new ConcurrentHashMap<>();

    @Override
    public void save(Block b) {
         db.put(b.getHashHex(), b);
        }
    @Override public Block findByHash(String hash) { return db.get(hash); }

    @Override
    public Iterable<Block> loadAll() { return db.values(); }
}
