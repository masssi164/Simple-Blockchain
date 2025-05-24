package de.flashyotter.blockchain_node.dto;

import java.util.List;

/**
 * Response containing raw-JSON blocks, ordered ascending by height.
 */
public record BlocksDto(List<String> rawBlocks) implements P2PMessageDto {}
