package de.flashyotter.blockchain_node.discovery;

import de.flashyotter.blockchain_node.dto.P2PMessageDto;

/** Response to a Ping */
public record PongDto(String fromId) implements P2PMessageDto { }
