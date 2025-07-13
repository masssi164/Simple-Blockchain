package de.flashyotter.blockchain_node.controller;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.NodeIdDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoint exposing this node's identifier. */
@RestController
@RequiredArgsConstructor
public class NodeController {
    private final NodeProperties props;

    @GetMapping("/node/id")
    public NodeIdDto id() {
        return new NodeIdDto(props.getId());
    }
}
