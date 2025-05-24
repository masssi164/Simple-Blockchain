package de.flashyotter.blockchain_node.storage;

import blockchain.core.model.Block;
import org.springframework.context.annotation.Primary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory store (non-persistent). */
@Primary
public class InMemoryBlockStore implements BlockStore {

    private final Map<String, Block> db = new ConcurrentHashMap<>();

    @Override 
    public void save(Block b) {
         db.put(b.getHashHex(), b); 
        }
    @Override public Block findByHash(String hash) { return db.get(hash); }
}
