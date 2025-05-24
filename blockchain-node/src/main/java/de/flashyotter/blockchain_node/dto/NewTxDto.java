package de.flashyotter.blockchain_node.dto;
public record NewTxDto(String rawTxJson)        implements P2PMessageDto {}
