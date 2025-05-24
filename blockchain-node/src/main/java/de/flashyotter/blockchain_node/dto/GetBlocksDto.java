package de.flashyotter.blockchain_node.dto;

/**
 * P2P request: “Send me every block where height &gt; {@code fromHeight}”.
 */
public record GetBlocksDto(int fromHeight) implements P2PMessageDto {}
