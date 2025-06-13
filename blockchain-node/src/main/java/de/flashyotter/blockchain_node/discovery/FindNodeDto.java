package de.flashyotter.blockchain_node.discovery;

import de.flashyotter.blockchain_node.dto.P2PMessageDto;

/** Query asking for peers close to the target ID */
public record FindNodeDto(String targetId) implements P2PMessageDto { }
