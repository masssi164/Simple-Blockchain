package de.flashyotter.blockchain_node.dto;

/**
 * Kademlia request asking a peer for nodes close to {@code nodeId}.
 */
public record FindNodeDto(String nodeId) implements P2PMessageDto {}
