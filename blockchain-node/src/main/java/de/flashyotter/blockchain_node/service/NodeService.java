package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates mem-pool, chain, mining and the P2P layer.
 */
@Service
@RequiredArgsConstructor
public class NodeService {

    private final Chain                        chain;
    private final MempoolService               mempool;
    private final MiningService                mining;
    private final P2PBroadcastService          broadcaster;
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
        broadcaster.broadcastBlock(new NewBlockDto(
            blockchain.core.serialization.JsonUtils.toJson(b)
        ), null);
    }

    /* ---------- transactions ---------- */

    public void submitTx(Transaction tx) {
        mempool.submit(tx, currentUtxo());
        broadcaster.broadcastTx(
            new NewTxDto(blockchain.core.serialization.JsonUtils.toJson(tx)),
            null
        );
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

    /**
     * Current confirmed UTXO set (only mined blocks).
     */
    public Map<String, TxOutput> currentUtxo() {
        return chain.getUtxoSnapshot();
    }

    /**
     * Current UTXO set including pending (unmined) transactions.
     * This ensures outgoing spends immediately reduce the reported balance.
     */
    public Map<String, TxOutput> currentUtxoIncludingPending() {
        // Start from the confirmed UTXOs
        Map<String, TxOutput> effective = new HashMap<>(chain.getUtxoSnapshot());

        // Apply each pending transaction in the mempool
        List<Transaction> pending = mempool.take(Integer.MAX_VALUE);
        for (Transaction tx : pending) {
            // Remove all inputs (they are now spent)
            for (TxInput input : tx.getInputs()) {
                effective.remove(input.getReferencedOutputId());
            }
            // Add all outputs (including the change output)
            String txHash = tx.calcHashHex();
            int index = 0;
            for (TxOutput output : tx.getOutputs()) {
                effective.put(output.id(txHash, index++), output);
            }
        }

        return effective;
    }

    public List<Block> blocksFromHeight(int from) {
        return chain.getBlocks().stream()
                    .filter(b -> b.getHeight() >= from)
                    .toList();
    }

    /**
     * Paginates blocks in descending order.
     *
     * @param page page index, 0 returns the most recent blocks
     * @param size number of blocks per page
     * @return list of blocks for the given page
     */
    public List<Block> blockPage(int page, int size) {
        List<Block> all = chain.getBlocks();
        int end   = all.size() - page * size;
        if (end <= 0) return List.of();
        int start = Math.max(0, end - size);
        return all.subList(start, end);
    }

    public Block latestBlock() {
        return chain.getLatest();
    }
}
