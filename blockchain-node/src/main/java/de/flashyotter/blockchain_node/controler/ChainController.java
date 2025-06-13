package de.flashyotter.blockchain_node.controler;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chain")
@RequiredArgsConstructor
public class ChainController {

    private final NodeService node;

    /** Latest block (tip of the best chain). */
    @GetMapping("/latest")
    public Block latest() {
        return node.latestBlock();
    }

    /** All blocks from a given height (inclusive). */
    @GetMapping
    public java.util.List<Block> blocks(@RequestParam(defaultValue = "0") int from) {
        return node.blocksFromHeight(from);
    }

    /**
     * Paginated list of blocks in descending order.
     */
    @GetMapping("/page")
    public java.util.List<Block> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return node.blockPage(page, size);
    }
}
