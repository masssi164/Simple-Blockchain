package de.flashyotter.blockchain_node.controler;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mining")
@RequiredArgsConstructor
public class MiningController {

    private final NodeService node;

    @PostMapping("/mine")
    public Block mine() {
        return node.mineNow();
    }
}
