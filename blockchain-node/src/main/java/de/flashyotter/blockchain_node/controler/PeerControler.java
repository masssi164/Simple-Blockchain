package de.flashyotter.blockchain_node.controler;
import java.net.URI;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.flashyotter.blockchain_node.service.PeerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
class PeerController {

    private final PeerService peerService;

    @GetMapping("/peers")
    Set<URI> list() { 
        return peerService.getPeers(); 
    }

    @PostMapping("/peers")
    Set<URI> register(@RequestBody Set<URI> incoming) {
        incoming.forEach(peerService::register);
        return peerService.getPeers();
    }
}
