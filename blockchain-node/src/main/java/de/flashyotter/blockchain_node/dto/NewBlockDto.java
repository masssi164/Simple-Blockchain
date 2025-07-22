package de.flashyotter.blockchain_node.dto;

/**
 * DTO for sending a new block.
 */
public record NewBlockDto(String rawBlockJson, String jwt) implements P2PMessageDto {
    public NewBlockDto(String rawBlockJson) {
        this(rawBlockJson, null);
    }
    
    @Override
    public String jwt() {
        return jwt;
    }
}
