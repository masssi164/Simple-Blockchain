package de.flashyotter.blockchain_node.dto;

public record PeerListDto(java.util.List<String> peers) implements P2PMessageDto {}
