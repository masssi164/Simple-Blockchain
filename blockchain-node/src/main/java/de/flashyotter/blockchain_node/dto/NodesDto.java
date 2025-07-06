package de.flashyotter.blockchain_node.dto;

import java.util.List;

/**
 * Response to {@link FindNodeDto} containing peer addresses.
 */
public record NodesDto(List<String> peers) implements P2PMessageDto {}
