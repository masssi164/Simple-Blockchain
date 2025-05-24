package blockchain.core.consensus;

/**
 * Static consensus parameters (≈ Bitcoin’s chain-params).
 * 
 * Block reward = INITIAL / 2^{⌊height / HALVING_INTERVAL⌋}
 */
public final class ConsensusParams {

    public static final long   TARGET_BLOCK_INTERVAL_MS  = 60_000;
    public static final int    DIFFICULTY_WINDOW         = 10;

    public static final int    MAX_BLOCK_SIZE_BYTES      = 1_000_000;
    public static final int    COINBASE_MATURITY         = 100;

    public static final int    HALVING_INTERVAL          = 210_000;
    public static final double INITIAL_BLOCK_REWARD      = 50.0;

    private ConsensusParams() { }

    /** Current subsidy (no fees). */
    public static double blockReward(int height) {
        int halvings = height / HALVING_INTERVAL;
        return INITIAL_BLOCK_REWARD / Math.pow(2, halvings);
    }
}
