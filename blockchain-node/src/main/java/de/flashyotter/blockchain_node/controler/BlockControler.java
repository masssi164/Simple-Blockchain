package de.flashyotter.blockchain_node.controler;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import blockchain.core.Architecture.Block;
import blockchain.core.Architecture.Chain;
import de.flashyotter.blockchain_node.service.ChainService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
class BlockController {

    private final ChainService chainService;

    @GetMapping("/blocks")
    List<Block> fullChain() { 
        Chain chain = chainService.getChain();
        return chain.getBlocks(); 
    }

    @PostMapping("/blocks")
    ResponseEntity<Void> receive(@RequestBody Block b) {
        return chainService.tryAdd(b) ? ResponseEntity.ok().build()
                                      : ResponseEntity.accepted().build();
    }
}
