package de.flashyotter.blockchain_node.p2p;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
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

import static org.mockito.Mockito.*;

class Libp2pHandshakeTest {

    @Test
    void closesOnVersionMismatch() throws Exception {
        Host host = mock(Host.class);
        NodeService node = mock(NodeService.class);
        NodeProperties props = new NodeProperties();
        props.setId("n1");

        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        PeerRegistry reg = new PeerRegistry();
        KademliaService kad = new KademliaService(table, reg, props);

        Libp2pService svc = new Libp2pService(host, props, new ObjectMapper(), node, kad);
        var cls = Class.forName(Libp2pService.class.getName() + "$ControlHandler");
        var ctor = cls.getDeclaredConstructor(svc.getClass());
        ctor.setAccessible(true);
        Object handler = ctor.newInstance(svc);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        var env = de.flashyotter.blockchain_node.p2p.proto.ProtoUtils.toProto(
                new HandshakeDto("x","0.0.1",0,"/ip4/1.1.1.1/tcp/1"), "");
        byte[] arr = env.toByteArray();
        ByteBuf buf = Unpooled.buffer(4 + arr.length).writeInt(arr.length).writeBytes(arr);
        when(ctx.close()).thenReturn(null);

        var method = cls.getDeclaredMethod("messageReceived", ChannelHandlerContext.class, ByteBuf.class);
        method.setAccessible(true);
        method.invoke(handler, ctx, buf);

        verify(ctx).close();
    }

    @Test
    void acceptsMatchingVersion() throws Exception {
        Host host = mock(Host.class);
        NodeService node = mock(NodeService.class);
        NodeProperties props = new NodeProperties();
        props.setId("n1");

        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        PeerRegistry reg = new PeerRegistry();
        KademliaService kad = new KademliaService(table, reg, props);

        Libp2pService svc = new Libp2pService(host, props, new ObjectMapper(), node, kad);
        var cls = Class.forName(Libp2pService.class.getName() + "$ControlHandler");
        var ctor = cls.getDeclaredConstructor(svc.getClass());
        ctor.setAccessible(true);
        Object handler = ctor.newInstance(svc);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        var env = de.flashyotter.blockchain_node.p2p.proto.ProtoUtils.toProto(
                new HandshakeDto("x","1.0.0",0,"/ip4/1.1.1.1/tcp/1"), "");
        byte[] arr = env.toByteArray();
        ByteBuf buf = Unpooled.buffer(4 + arr.length).writeInt(arr.length).writeBytes(arr);
        when(ctx.close()).thenReturn(null);

        var method = cls.getDeclaredMethod("messageReceived", ChannelHandlerContext.class, ByteBuf.class);
        method.setAccessible(true);
        method.invoke(handler, ctx, buf);

        verify(ctx, never()).close();
    }
}
