package de.flashyotter.blockchain_node.controller;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.NodeIdDto;
import de.flashyotter.blockchain_node.dto.PeerIdDto;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoint exposing this node's identifier. */
@RestController
@RequiredArgsConstructor
public class NodeController {
    private final NodeProperties props;
    private final Libp2pService libp2p;

    @GetMapping("/node/id")
    public NodeIdDto id() {
        return new NodeIdDto(props.getId());
    }

    @GetMapping("/node/peer-id")
    public PeerIdDto peerId() {
        return new PeerIdDto(libp2p.peerId());
    }
}
