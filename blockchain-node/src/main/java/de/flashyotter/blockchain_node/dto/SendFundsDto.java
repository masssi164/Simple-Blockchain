package de.flashyotter.blockchain_node.dto;

/**
 * Payload for POST /api/wallet/send.
 *
 * @param recipient base64-encoded EC-public-key
 * @param amount    coins to transfer
 */
public record SendFundsDto(String recipient, double amount) {}
