package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.FindNodeDto;
import de.flashyotter.blockchain_node.dto.NodesDto;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import io.libp2p.core.Host;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Libp2pKademliaHandlerTest {

    @Test
    void findNodeRespondsWithNodes() throws Exception {
        Host host = mock(Host.class);
        NodeService node = mock(NodeService.class);
        NodeProperties props = new NodeProperties();
        props.setId("abc");

        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        PeerRegistry reg = new PeerRegistry();
        KademliaService kad = new KademliaService(table, reg, props);
        kad.store(new Peer("x", 1));

        Libp2pService svc = new Libp2pService(host, props, new ObjectMapper(), node, kad);
        // instantiate handler via reflection
        var cls = Class.forName(Libp2pService.class.getName() + "$ControlHandler");
        var ctor = cls.getDeclaredConstructor(svc.getClass());
        ctor.setAccessible(true);
        Object handler = ctor.newInstance(svc);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ByteBuf buf = Unpooled.copiedBuffer(new ObjectMapper()
                .writeValueAsString(new FindNodeDto("abc")), StandardCharsets.UTF_8);
        when(ctx.writeAndFlush(any())).thenReturn(null);

        var method = cls.getDeclaredMethod("messageReceived", ChannelHandlerContext.class, ByteBuf.class);
        method.setAccessible(true);
        method.invoke(handler, ctx, buf);

        verify(ctx).writeAndFlush(any());
        var captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(ctx).writeAndFlush(captor.capture());
        ByteBuf out = (ByteBuf) captor.getValue();
        NodesDto resp = new ObjectMapper().readValue(out.toString(StandardCharsets.UTF_8), NodesDto.class);
        assertEquals(List.of("x:1"), resp.peers());
    }
}
