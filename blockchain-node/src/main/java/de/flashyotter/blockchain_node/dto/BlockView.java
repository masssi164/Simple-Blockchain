package de.flashyotter.blockchain_node.dto;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;

import java.util.List;

/**
 * Lightweight JSON serialisable view of a block used by the JSON-RPC facade.
 */
public record BlockView(
        int height,
        int compactDifficultyBits,
        String hashHex,
        String previousHashHex,
        long timeMillis,
        int nonce,
        String merkleRootHex,
        List<Transaction> txList
) {

    public static BlockView from(Block block) {
        return new BlockView(
                block.getHeight(),
                block.getCompactDifficultyBits(),
                block.getHashHex(),
                block.getPreviousHashHex(),
                block.getTimeMillis(),
                block.getNonce(),
                block.getMerkleRootHex(),
                block.getTxList()
        );
    }
}
