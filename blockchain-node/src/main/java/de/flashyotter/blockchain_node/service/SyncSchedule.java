package de.flashyotter.blockchain_node.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import blockchain.core.Architecture.Block;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
class SyncScheduler {

    private final PeerService  peers;
    private final ChainService chain;
    private final RestTemplate rest;

    /** 30 000 ms Default, sonst über application.yaml steuerbar            */
    @Scheduled(fixedDelayString = "${node.sync-interval-ms:30000}")
    void poll() {
        peers.getPeers().forEach(p -> {
            try {
                var resp = rest.exchange(
                        p.resolve("/blocks"), HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Block>>() {});
                chain.replace(resp.getBody());
            } catch (Exception ex) {
                log.debug("sync {} failed: {}", p, ex.getMessage());
            }
        });
    }
}
