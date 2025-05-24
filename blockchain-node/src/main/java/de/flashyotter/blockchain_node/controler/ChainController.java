package de.flashyotter.blockchain_node.controler;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only chain endpoints (health & latest header). */
@RestController
@RequestMapping("/api/chain")
@RequiredArgsConstructor
public class ChainController {

    private final NodeService node;

    @GetMapping("/latest")
    public Block latest() {
        return node.latestBlock();
    }
}
