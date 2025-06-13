package de.flashyotter.blockchain_node.discovery;

import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import java.util.List;

/** Response carrying a list of peers */
public record NodesDto(List<String> peers) implements P2PMessageDto { }
