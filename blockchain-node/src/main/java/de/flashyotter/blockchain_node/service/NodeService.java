package de.flashyotter.blockchain_node.service;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import lombok.RequiredArgsConstructor;
import blockchain.core.exceptions.BlockchainException;
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

    /** Snapshot of the currently active chain to detect re-orgs. */
    private java.util.List<Block> chainSnapshot = new java.util.ArrayList<>();

    /* ---------- mining ---------- */

    public Mono<Block> mineNow() {
        return Mono.fromCallable(mining::mine)
                   .subscribeOn(Schedulers.boundedElastic())
                   .doOnNext(this::onLocalBlockMined);
    }

    private void onLocalBlockMined(Block b) {
        java.util.List<Block> prev = new java.util.ArrayList<>(chainSnapshot);

        chain.addBlock(b);
        store.save(b);
        mempool.purge(b.getTxList());
        handleReorg(prev, chain.getBlocks());

        broadcaster.broadcastBlock(new NewBlockDto(
            blockchain.core.serialization.JsonUtils.toJson(b)
        ), null);
    }

    /* ---------- transactions ---------- */

    public void submitTx(Transaction tx) {
        mempool.submit(tx, currentUtxoIncludingPending());
        broadcaster.broadcastTx(
            new NewTxDto(blockchain.core.serialization.JsonUtils.toJson(tx)),
            null
        );
    }

    public void acceptExternalTx(Transaction tx) {
        mempool.submit(tx, currentUtxoIncludingPending());
    }

    /* ---------- blocks from peers ---------- */

    public void acceptExternalBlock(Block blk) {
        java.util.List<Block> prev = new java.util.ArrayList<>(chainSnapshot);

        chain.addBlock(blk);
        store.save(blk);
        mempool.purge(blk.getTxList());
        handleReorg(prev, chain.getBlocks());
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

    /**
     * Returns the most recent transactions involving the given address either
     * as sender or recipient.
     */
    public List<Transaction> walletHistory(String address, int limit) {
        List<Block> blocks = chain.getBlocks();
        java.util.List<Transaction> result = new java.util.ArrayList<>();

        outer:
        for (int i = blocks.size() - 1; i >= 0; i--) {
            for (Transaction tx : blocks.get(i).getTxList()) {
                boolean isSender = tx.getInputs().stream()
                    .anyMatch(in -> blockchain.core.crypto.AddressUtils
                        .publicKeyToAddress(in.getSender()).equals(address));
                boolean isRecipient = tx.getOutputs().stream()
                    .anyMatch(out -> out.recipientAddress().equals(address));
                if (isSender || isRecipient) {
                    result.add(tx);
                    if (result.size() >= limit) break outer;
                }
            }
        }

        return result;
    }

    public Block latestBlock() {
        return chain.getLatest();
    }

    /* ------------------------------------------------------------------ */
    /* Re-org handling                                                    */
    /* ------------------------------------------------------------------ */

    private void handleReorg(List<Block> previous, List<Block> current) {
        int idx = 0;
        while (idx < previous.size() &&
               idx < current.size() &&
               previous.get(idx).getHashHex().equals(current.get(idx).getHashHex())) {
            idx++;
        }

        for (Block removed : previous.subList(idx, previous.size())) {
            removed.getTxList().stream().skip(1).forEach(tx -> {
                try {
                    mempool.submit(tx, currentUtxo());
                } catch (BlockchainException ignored) {
                    // invalid on new chain; drop silently
                }
            });
        }

        chainSnapshot = new java.util.ArrayList<>(current);
    }
}
