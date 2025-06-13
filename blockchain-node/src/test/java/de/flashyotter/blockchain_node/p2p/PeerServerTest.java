package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flashyotter.blockchain_node.dto.BlocksDto;
import de.flashyotter.blockchain_node.dto.GetBlocksDto;
import de.flashyotter.blockchain_node.dto.NewBlockDto;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.dto.P2PMessageDto;
import de.flashyotter.blockchain_node.dto.PeerListDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.P2PBroadcastService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.discovery.PeerDiscoveryService;

class PeerServerTest {

    @Mock ObjectMapper mapper;
    @Mock NodeService nodeService;
    @Mock PeerRegistry registry;
    @Mock P2PBroadcastService broadcastService;
    @Mock NodeProperties props;
    @Mock PeerDiscoveryService discovery;
    @Mock WebSocketSession session;

    @Captor ArgumentCaptor<TextMessage> messageCaptor;

    PeerServer peerServer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        peerServer = new PeerServer(mapper, nodeService, registry, broadcastService, props, discovery);
    }

    @Test
    void handleNewTxDto_forwardsToNodeAndBroadcasts() throws Exception {
        // given
        String rawTxJson = "{\"dummy\":\"tx\"}";
        NewTxDto dto = new NewTxDto(rawTxJson);

        when(mapper.readValue(anyString(), eq(P2PMessageDto.class))).thenReturn(dto);
        when(mapper.readValue(rawTxJson, blockchain.core.model.Transaction.class))
            .thenReturn(new blockchain.core.model.Transaction());

        // when
        peerServer.handleTextMessage(
            session,
            new TextMessage("{\"type\":\"NewTxDto\",\"rawTxJson\":" + rawTxJson + "}")
        );

        // then
        verify(nodeService).acceptExternalTx(any());
        verify(broadcastService).broadcastTx(eq(dto), isNull());
    }

    @Test
    void handleNewBlockDto_forwardsToNodeAndBroadcasts() throws Exception {
        String rawBlockJson = "{\"dummy\":\"block\"}";
        NewBlockDto dto = new NewBlockDto(rawBlockJson);

        when(mapper.readValue(anyString(), eq(P2PMessageDto.class))).thenReturn(dto);
        when(mapper.readValue(rawBlockJson, blockchain.core.model.Block.class))
            .thenReturn(new blockchain.core.model.Block(0, "0".repeat(64), List.of(), 0));

        peerServer.handleTextMessage(
            session,
            new TextMessage("{\"type\":\"NewBlockDto\",\"rawBlockJson\":" + rawBlockJson + "}")
        );

        verify(nodeService).acceptExternalBlock(any());
        verify(broadcastService).broadcastBlock(eq(dto), isNull());
    }

    @Test
    void handleGetBlocksDto_returnsBlocksList() throws Exception {
        // prepare
        GetBlocksDto req = new GetBlocksDto(5);
        List<blockchain.core.model.Block> blocks = List.of(
            new blockchain.core.model.Block(5, "0".repeat(64), List.of(), 0)
        );

        when(mapper.readValue(anyString(), eq(P2PMessageDto.class))).thenReturn(req);
        when(nodeService.blocksFromHeight(5)).thenReturn(blocks);
        when(mapper.writeValueAsString(any(BlocksDto.class)))
             .thenReturn("{\"rawBlocks\":[\"someJson\"]}");

        peerServer.handleTextMessage(
            session,
            new TextMessage("{\"type\":\"GetBlocksDto\",\"fromHeight\":5}")
        );

        verify(session).sendMessage(messageCaptor.capture());
        String sent = messageCaptor.getValue().getPayload();
        assert sent.contains("\"rawBlocks\"");
    }

    @Test
    void handlePeerListDto_addsToRegistry() throws Exception {
        PeerListDto dto = new PeerListDto(List.of("host1:1234", "host2:5678"));

        when(mapper.readValue(anyString(), eq(P2PMessageDto.class))).thenReturn(dto);

        peerServer.handleTextMessage(
            session,
            new TextMessage("{\"type\":\"PeerListDto\",\"peers\":[\"host1:1234\",\"host2:5678\"]}")
        );

        // registry.addAll should be called with exactly two Peer instances
        verify(registry).addAll(argThat(iter -> {
            return ((java.util.Collection<?>) iter).size() == 2;
       }));
    }
}
