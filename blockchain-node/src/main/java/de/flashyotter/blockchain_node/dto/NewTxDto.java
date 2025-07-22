package de.flashyotter.blockchain_node.dto;

/**
 * DTO for sending a new transaction.
 */
public record NewTxDto(String rawTxJson, String jwt) implements P2PMessageDto {
    public NewTxDto(String rawTxJson) {
        this(rawTxJson, null);
    }
    
    @Override
    public String jwt() {
        return jwt;
    }
}
