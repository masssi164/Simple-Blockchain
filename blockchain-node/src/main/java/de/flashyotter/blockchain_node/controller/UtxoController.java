package de.flashyotter.blockchain_node.controller;

import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utxo")
@RequiredArgsConstructor
public class UtxoController {

    private final NodeService node;

    @GetMapping
    public List<Map<String, Object>> byAddress(@RequestParam String address) {
        if (address == null || address.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "address must be provided");
        }

        Map<String, TxOutput> utxo = node.currentUtxoIncludingPending();
        return utxo.entrySet().stream()
                .filter(e -> e.getValue().recipientAddress().equals(address))
                .map(e -> Map.<String, Object>of(
                        "id", e.getKey(),
                        "value", e.getValue().value(),
                        "recipientAddress", e.getValue().recipientAddress()))
                .toList();
    }
}
