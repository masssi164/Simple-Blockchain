package blockchain.core.consensus;

/** Network-wide constants. */
public final class ConsensusParams {
    public static final long   TARGET_BLOCK_INTERVAL_MS = 60_000;
    public static final int    DIFFICULTY_WINDOW        = 10;
    public static final int    MAX_BLOCK_SIZE_BYTES     = 1_000_000;
    public static final int    COINBASE_MATURITY        = 100;
    public static final int    HALVING_INTERVAL         = 210_000;
    public static final double INITIAL_BLOCK_REWARD     = 50.0;
    public static final int    RETARGET_SPAN            = 10;             // blocks
    public static final long   RETARGET_TIMESPAN_MS     = TARGET_BLOCK_INTERVAL_MS * RETARGET_SPAN;

    private ConsensusParams() { }

    /** Bitcoin-style reward schedule (halves every 210 000 blocks). */
    public static double blockReward(int height) {
        int halvings = height / HALVING_INTERVAL;
        return INITIAL_BLOCK_REWARD / Math.pow(2, halvings);
    }
}
