package de.flashyotter.blockchain_node.storage;

import blockchain.core.model.Block;

/**
 * Simple interface; LevelDB/in-mem implementations possible.
 */
public interface BlockStore {
    void save(Block b);
    Block findByHash(String hash);
}
