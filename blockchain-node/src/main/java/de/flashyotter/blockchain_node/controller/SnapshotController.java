package de.flashyotter.blockchain_node.controller;

import de.flashyotter.blockchain_node.config.NodeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Serves UTXO snapshot files to peers. */
@RestController
@RequiredArgsConstructor
public class SnapshotController {
    private final NodeProperties props;

    @GetMapping("/snap/{height}")
    public ResponseEntity<Resource> snapshot(@PathVariable int height) throws IOException {
        Path file = Path.of(props.getDataPath(), "snapshots",
                String.format("%07d.json.gz", height));
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        InputStreamResource res = new InputStreamResource(Files.newInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }
}
