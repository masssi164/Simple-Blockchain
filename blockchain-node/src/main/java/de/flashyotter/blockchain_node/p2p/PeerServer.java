// blockchain-node/src/main/java/de/flashyotter/blockchain_node/p2p/PeerServer.java
package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.dto.*;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;
import de.flashyotter.blockchain_node.p2p.ConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component @RequiredArgsConstructor @Slf4j
public class PeerServer extends TextWebSocketHandler {

    private final ObjectMapper        mapper;
    private final NodeService         node;
    private final PeerRegistry        registry;
    private final P2PBroadcastService broadcast;
    private final NodeProperties      props;
    private final PeerDiscoveryService discovery;
    private final ConnectionManager   connectionManager;

    private static final String PROTOCOL_VER = "0.4.0";

    @Override
    @SneakyThrows
    public void afterConnectionEstablished(WebSocketSession session) {
        // send our handshake immediately
        HandshakeDto hello = new HandshakeDto(props.getId(), PROTOCOL_VER);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(hello)));

        java.net.InetSocketAddress addr = (java.net.InetSocketAddress) session.getRemoteAddress();
        Peer peer = new Peer(addr.getHostString(), addr.getPort());
        connectionManager.registerServerSession(peer, session);
    }

    @Override
    @SneakyThrows
    public void handleTextMessage(WebSocketSession sess, TextMessage msg) {

        P2PMessageDto dto = mapper.readValue(msg.getPayload(), P2PMessageDto.class);
        java.net.InetSocketAddress addr = (java.net.InetSocketAddress) sess.getRemoteAddress();
        Peer peer = new Peer(addr.getHostString(), addr.getPort());
        connectionManager.emitInbound(peer, dto);
    }
}
