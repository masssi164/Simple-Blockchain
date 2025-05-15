package de.flashyotter.blockchain_node.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.service.PeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeerWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final PeerService  peerService;               // Cycles sind jetzt gelöst
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    /* ---- Lifecycle ----------------------------------------------------- */

    @Override
    public void afterConnectionEstablished(WebSocketSession s) {
        sessions.add(s);
    }

    @Override
    public void handleTextMessage(WebSocketSession s, TextMessage msg) throws IOException {
        URI peer = mapper.readValue(msg.getPayload(), URI.class);
        peerService.register(peer);
        broadcast(peer, s);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        sessions.remove(s);
    }

    /* ---- Helper -------------------------------------------------------- */

    /** schickt die neue Peer-URI an alle offenen Sessions außer der Ursprungs-Session */
    private void broadcast(URI peer, WebSocketSession origin) throws IOException {
        String json = mapper.writeValueAsString(peer);
        for (WebSocketSession s : sessions)
            if (s.isOpen() && s != origin)
                s.sendMessage(new TextMessage(json));
    }
}
