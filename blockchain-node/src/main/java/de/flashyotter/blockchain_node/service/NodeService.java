package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.storage.BlockStore;
import blockchain.core.serialization.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import blockchain.core.model.TxOutput;

/**
 * Main façade used by REST and P2P layers.
 */
@Service
@RequiredArgsConstructor
public class NodeService {

    private final Chain               chain;
    private final MempoolService      mempool;
    private final MiningService       mining;
    private final BlockStore          store;
    private final P2PBroadcastService broadcaster;

    /* ───────────────── chain view ─────────────────────────────────── */

    public Block latestBlock() { return chain.getLatest(); }

    public List<Block> blocksFromHeight(int height) {
        return chain.getBlocks().stream()
                    .filter(b -> b.getHeight() > height)
                    .toList();
    }


    
    public void acceptExternalTx(Transaction tx) {
        mempool.submit(tx, chain.getUtxoSnapshot());
    }

    /* ───────────────── mining ─────────────────────────────────────── */

    public Block mineNow() {
        Block b = mining.mine();
        chain.addBlock(b);
        store.save(b);
        broadcaster.broadcastBlock(new NewBlockDto(JsonUtils.toJson(b)), null);
        mempool.purge(b.getTxList());
        return b;
    }

    /* ───────────────── external blocks ────────────────────────────── */

    public void acceptExternalBlock(Block blk) {
        chain.addBlock(blk);
        store.save(blk);
        mempool.purge(blk.getTxList());
    }

    public Map<String, TxOutput> currentUtxo() {
        try {
            var f = Chain.class.getDeclaredField("utxo"); f.setAccessible(true);
            //noinspection unchecked
            return Map.copyOf((Map<String, TxOutput>) f.get(chain));
        } catch (Exception e) { return Map.of(); }
    }

    /* submitTx, mineNow … remain identical except: */
    public void submitTx(Transaction tx) {
        mempool.submit(tx, currentUtxo());
        broadcaster.broadcastTx(new NewTxDto(
                blockchain.core.serialization.JsonUtils.toJson(tx)), null);
    }

}
