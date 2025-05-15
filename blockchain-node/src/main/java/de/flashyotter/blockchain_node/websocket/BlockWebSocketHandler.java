package de.flashyotter.blockchain_node.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.Architecture.Block;
import de.flashyotter.blockchain_node.service.ChainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockWebSocketHandler extends TextWebSocketHandler {

    private final ChainService chainService;
    private final ObjectMapper mapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession s) { sessions.add(s); }

    @Override
    public void handleTextMessage(WebSocketSession s, TextMessage msg) throws IOException {
        Block b = mapper.readValue(msg.getPayload(), Block.class);
        if (chainService.tryAdd(b)) broadcast(b, s);
        else log.warn("Block {} rejected – prevHash mismatch", b.getIndex());
    }

    private void broadcast(Block b, WebSocketSession origin) throws IOException {
        String json = mapper.writeValueAsString(b);
        for (WebSocketSession s : sessions)
            if (s.isOpen() && s != origin) s.sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        sessions.remove(s);
    }
}
