package de.flashyotter.blockchain_node.service;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.websocket.BlockWebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeerService {

    private final RestTemplate           rest;
    private final NodeProperties         props;
    private final BlockWebSocketHandler  blockWs;

    /* peers live set */
    private final Set<URI> peers = ConcurrentHashMap.newKeySet();

    @PostConstruct
    void init() {
        peers.addAll(props.getBootstrap());
        boolean any = props.getBootstrap().stream()
                           .map(this::handshake).anyMatch(Boolean::booleanValue);
        if (!any) log.info("Standalone – acting as genesis node");
    }

    public Set<URI> getPeers() { return Set.copyOf(peers); }

    public void register(URI peer) {
        if (peers.add(peer)) handshake(peer);
    }

    /* ---------- intern ---------- */

    private boolean handshake(URI peer) {
        try {
            var body = new HttpEntity<>(Set.of(self()));
            var resp = rest.exchange(peer.resolve("/peers"),
                       HttpMethod.POST, body,
                       new ParameterizedTypeReference<Set<URI>>() {});
            peers.addAll(resp.getBody());
            connectWs(peer);
            return true;
        } catch (Exception ex) {
            log.warn("Peer {} unreachable: {}", peer, ex.getMessage());
            peers.remove(peer);
            return false;
        }
    }

    private void connectWs(URI peer) {
        var client  = new StandardWebSocketClient();
        var headers = new WebSocketHttpHeaders();

        // 1️⃣ Blöcke
        CompletableFuture<WebSocketSession> blocks =
                client.execute(blockWs, headers, peer.resolve("/ws/blocks"));

        // 2️⃣ Peers – Minimal-Handler, um neue URIs einzusammeln
        CompletableFuture<WebSocketSession> peers =
                client.execute(new MinimalPeerHandler(), headers, peer.resolve("/ws/peers"));

        // optional: Fehler loggen
        blocks.exceptionally(ex -> { log.warn("WS /blocks → {}", ex.getMessage()); return null; });
        peers .exceptionally(ex -> { log.warn("WS /peers  → {}", ex.getMessage()); return null; });
    }
    
    private URI self() {
        String port = System.getProperty("server.port", "8080");
        return URI.create("http://" + props.getHost() + ":" + port);
    }

    private class MinimalPeerHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage msg) {
        register(URI.create(msg.getPayload()));
    }
}

}
