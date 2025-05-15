package blockchain.core.exceptions;

public class BlockchainException extends RuntimeException {
    public BlockchainException(String msg) { super(msg); }
    public BlockchainException(String msg, Throwable cause) { super(msg, cause); }
}
