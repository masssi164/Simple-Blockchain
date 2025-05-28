package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

/** Coordinates mem-pool, chain, mining and the P2P layer. */
@Service
@RequiredArgsConstructor
public class NodeService {

    private final Chain               chain;
    private final MempoolService      mempool;
    private final MiningService       mining;
    private final P2PBroadcastService broadcaster;
    private final de.flashyotter.blockchain_node.storage.BlockStore store;

    /* ---------- mining ---------- */

    public Mono<Block> mineNow() {
        return Mono.fromCallable(mining::mine)
                   .subscribeOn(Schedulers.boundedElastic())
                   .doOnNext(this::onLocalBlockMined);
    }

    private void onLocalBlockMined(Block b) {
        chain.addBlock(b);
        store.save(b);
        mempool.purge(b.getTxList());
        broadcaster.broadcastBlock(new NewBlockDto(JsonUtils.toJson(b)), null);
    }

    /* ---------- transactions ---------- */

    public void submitTx(Transaction tx) {
        mempool.submit(tx, currentUtxo());
        broadcaster.broadcastTx(new NewTxDto(JsonUtils.toJson(tx)), null);
    }

    public void acceptExternalTx(Transaction tx) {
        mempool.submit(tx, currentUtxo());
    }

    /* ---------- blocks from peers ---------- */

    public void acceptExternalBlock(Block blk) {
        chain.addBlock(blk);
        store.save(blk);
        mempool.purge(blk.getTxList());
    }

    /* ---------- simple getters ---------- */

    public Map<String, TxOutput> currentUtxo() {
        return chain.getUtxoSnapshot();
    }

    public List<Block> blocksFromHeight(int from) {
        return chain.getBlocks().stream()
                    .filter(b -> b.getHeight() >= from)
                    .toList();
    }

    public Block latestBlock() {
        return chain.getLatest();
    }
}
