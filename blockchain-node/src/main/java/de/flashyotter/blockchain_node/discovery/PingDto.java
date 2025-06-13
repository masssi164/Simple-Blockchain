package de.flashyotter.blockchain_node.discovery;

import de.flashyotter.blockchain_node.dto.P2PMessageDto;

/** Simple reachability check message */
public record PingDto(String fromId) implements P2PMessageDto { }
