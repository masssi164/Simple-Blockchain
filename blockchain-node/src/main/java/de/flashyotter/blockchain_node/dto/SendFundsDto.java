package de.flashyotter.blockchain_node.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Payload for POST /api/wallet/send.
 *
 * @param recipient base64-encoded EC-public-key
 * @param amount    coins to transfer
 */
public record SendFundsDto(
    @NotBlank String recipient,
    @Positive double amount
) {}
