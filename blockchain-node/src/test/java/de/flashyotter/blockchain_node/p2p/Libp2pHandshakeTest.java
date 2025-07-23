package de.flashyotter.blockchain_node.p2p;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.HandshakeDto;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.p2p.P2PProtoMapper;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import de.flashyotter.blockchain_node.service.TablePeerStore;
import io.libp2p.core.Host;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        PeerRegistry reg = new PeerRegistry(props);
        KademliaService kad = new KademliaService(table, new TablePeerStore(table), reg, props);
        org.springframework.web.reactive.function.client.ExchangeFunction fx = req ->
                reactor.core.publisher.Mono.just(
                        org.springframework.web.reactive.function.client.ClientResponse
                                .create(org.springframework.http.HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body("{\"peerId\":\"id-123\"}")
                                .build());
        WebClient client = WebClient.builder().exchangeFunction(fx).build(); // unused

        Libp2pService svc = new Libp2pService(host, props, node, kad);
        var cls = Class.forName(Libp2pService.class.getName() + "$ControlHandler");
        var ctor = cls.getDeclaredConstructor(svc.getClass());
        ctor.setAccessible(true);
        Object handler = ctor.newInstance(svc);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        var msg = de.flashyotter.blockchain_node.p2p.P2PProtoMapper.toProto(
                new HandshakeDto("x","peer1","0.0.1",0,0));
        byte[] data = msg.toByteArray();
        ByteBuf buf = Unpooled.buffer(4 + data.length).writeIntLE(data.length).writeBytes(data);
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
        PeerRegistry reg = new PeerRegistry(props);
        KademliaService kad = new KademliaService(table, new TablePeerStore(table), reg, props);
        org.springframework.web.reactive.function.client.ExchangeFunction fx2 = req ->
                reactor.core.publisher.Mono.just(
                        org.springframework.web.reactive.function.client.ClientResponse
                                .create(org.springframework.http.HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body("{\"peerId\":\"id-456\"}")
                                .build());
        WebClient client = WebClient.builder().exchangeFunction(fx2).build(); // unused

        Libp2pService svc = new Libp2pService(host, props, node, kad);
        var cls = Class.forName(Libp2pService.class.getName() + "$ControlHandler");
        var ctor = cls.getDeclaredConstructor(svc.getClass());
        ctor.setAccessible(true);
        Object handler = ctor.newInstance(svc);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        io.netty.channel.Channel ch = mock(io.netty.channel.Channel.class);
        when(ctx.channel()).thenReturn(ch);
        java.net.InetSocketAddress addr = new java.net.InetSocketAddress("1.2.3.4", 10000);
        when(ch.remoteAddress()).thenReturn(addr);

        var msg = de.flashyotter.blockchain_node.p2p.P2PProtoMapper.toProto(
                new HandshakeDto("x","peer2","1.0.0",7000,7001));
        byte[] data = msg.toByteArray();
        ByteBuf buf = Unpooled.buffer(4 + data.length).writeIntLE(data.length).writeBytes(data);
        when(ctx.close()).thenReturn(null);

        var method = cls.getDeclaredMethod("messageReceived", ChannelHandlerContext.class, ByteBuf.class);
        method.setAccessible(true);
        method.invoke(handler, ctx, buf);

        verify(ctx, never()).close();
        assertTrue(reg.all().contains(new Peer("1.2.3.4", 7001, 7000, "peer2")));
    }
}
