package blockchain.core.consensus;

/**
 * Additional constants and configuration values used throughout the blockchain.
 * These are values that don't directly affect consensus but are important for
 * implementation details.
 */
public final class Constants {
    
    private Constants() {
        // Utility class, prevent instantiation
    }
    
    /* ──────────────────── Genesis Block ──────────────────── */
    
    /** Genesis block has no predecessor, represented by 64 zeros */
    public static final String GENESIS_PREV_HASH = "0".repeat(64);
    
    /* ──────────────────── Transaction Limits ──────────────────── */
    
    /** Maximum transactions to pull from mempool when building a block */
    public static final int DEFAULT_TX_PER_BLOCK = 500;
    
    /** Maximum size for a single transaction in bytes */
    public static final int MAX_TRANSACTION_SIZE_BYTES = 100_000; // 100KB
    
    /* ──────────────────── P2P and Sync ──────────────────── */
    
    /** Default batch size when syncing blocks from peers */
    public static final int DEFAULT_SYNC_BATCH_SIZE = 100;
    
    /** Maximum reconnection delay for WebSocket in milliseconds */
    public static final long WS_MAX_RECONNECT_MS = 30_000; // 30 seconds
    
    /** Default initial reconnection delay in milliseconds */
    public static final long WS_INITIAL_RECONNECT_MS = 1_000; // 1 second
    
    /* ──────────────────── Storage ──────────────────── */
    
    /** Default number of recent blocks to keep for potential forks */
    public static final int DEFAULT_FORK_HISTORY_DEPTH = 1000;
    
    /* ──────────────────── Mining ──────────────────── */
    
    /** Default number of mining threads if not configured */
    public static final int DEFAULT_MINING_THREADS = 4;
    
    /** Default mining timeout in seconds */
    public static final int DEFAULT_MINING_TIMEOUT_SECONDS = 300; // 5 minutes
    
    /* ──────────────────── Precision ──────────────────── */
    
    /** Epsilon for floating-point comparisons (equivalent to 1 satoshi) */
    public static final double EPSILON = 0.00000001;
}
