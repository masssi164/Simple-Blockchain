package de.flashyotter.blockchain_node.controler;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mining")
@RequiredArgsConstructor
public class MiningController {

    private final NodeService node;

    @PostMapping("/mine")
    public Mono<Block> mine() {
        return node.mineNow();
    }
}
