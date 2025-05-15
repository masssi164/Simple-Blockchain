package de.flashyotter.blockchain_node.service;

import java.util.List;

import org.springframework.stereotype.Service;

import blockchain.core.Architecture.Block;
import blockchain.core.Architecture.Chain;
import lombok.Getter;

@Service
public class ChainService {

    @Getter
    private final Chain chain = new Chain();          // enthält Genesis bis erste Synchronisierung

    public synchronized boolean tryAdd(Block b) {
        Block latest = chain.getLatest();
        if (b.getIndex() != latest.getIndex() + 1) return false;
        if (!b.getPrevHash().equals(latest.getHash())) return false;

        chain.addBlock(b);          // nutzt validateTx & mining aus core
        return true;
    }

    public synchronized void replace(List<Block> other) {
        if (other.size() > chain.getBlocks().size()) {
            chain.getBlocks().clear();
            chain.getBlocks().addAll(other);
        }
    }
}

