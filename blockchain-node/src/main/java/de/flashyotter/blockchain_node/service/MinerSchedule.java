package de.flashyotter.blockchain_node.service;
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("#{ @nodeProperties.isMiner() }")
@Slf4j
class MiningScheduler {

    private final NodeProperties props;
    private final MempoolService mempool;
    private final ChainService   chain;
    private final PeerService    peers;

    @Scheduled(fixedDelayString = "#{@nodeProperties.mining.intervalMs}")
    void mineIfNeeded() {
        if (!mempool.hasWork()) return;

        List<Transaction> txs = mempool.drain();
        Block block = chain.mineNextBlock(txs);
        peers.broadcastBlock(block);
        log.info("⛏️ Mined block {} ({} TX)", block.getIndex(), txs.size());
    }
}
