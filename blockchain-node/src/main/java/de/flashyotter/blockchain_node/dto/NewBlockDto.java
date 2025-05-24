package de.flashyotter.blockchain_node.dto;
public record NewBlockDto(String rawBlockJson)  implements P2PMessageDto {}
