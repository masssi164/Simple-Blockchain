package de.flashyotter.blockchain_node.controler;

import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tx")
@RequiredArgsConstructor
public class TxController {

    private final NodeService node;

    @PostMapping
    public ResponseEntity<Transaction> submit(@RequestBody Transaction tx) {
        node.submitTx(tx);
        return ResponseEntity.accepted().body(tx);
    }
}
