package de.flashyotter.blockchain_node.p2p;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.libp2p.core.Host;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

class Libp2pJwtAuthTest {

    private ByteBuf buildMessage(String jwt) {
        Transaction tx = new Transaction();
        tx.getOutputs().add(new TxOutput(1.0, "addr"));
        NewTxDto dto = new NewTxDto(JsonUtils.toJson(tx));
        P2PMessage msg = P2PProtoMapper.toProto(dto).toBuilder().setJwt(jwt).build();
        byte[] arr = msg.toByteArray();
        return Unpooled.buffer(4 + arr.length).writeIntLE(arr.length).writeBytes(arr);
    }

    private Object createTxHandler(Libp2pService svc) throws Exception {
        var cls = Class.forName(Libp2pService.class.getName() + "$TxHandler");
        var ctor = cls.getDeclaredConstructor(svc.getClass());
        ctor.setAccessible(true);
        return ctor.newInstance(svc);
    }

    @Test
    void closesOnInvalidJwt() throws Exception {
        Host host = mock(Host.class);
        NodeService node = mock(NodeService.class);
        NodeProperties props = new NodeProperties();
        props.setId("n1");
        props.setJwtSecret("secret123456789012345678901234567890");
        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        KademliaService kad = new KademliaService(table, new PeerRegistry(), props);
        Libp2pService svc = new Libp2pService(host, props, node, kad);

        Object handler = createTxHandler(svc);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ByteBuf buf = buildMessage("bad.jwt");
        var m = handler.getClass().getMethod("messageReceived", ChannelHandlerContext.class, ByteBuf.class);
        m.setAccessible(true);
        m.invoke(handler, ctx, buf);
        verify(ctx).close();
        verify(node, never()).acceptExternalTx(any());
    }

    @Test
    void acceptsValidJwt() throws Exception {
        Host host = mock(Host.class);
        NodeService node = mock(NodeService.class);
        NodeProperties props = new NodeProperties();
        props.setId("n1");
        props.setJwtSecret("secret123456789012345678901234567890");
        KademliaRoutingTable<Peer> table = KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        KademliaService kad = new KademliaService(table, new PeerRegistry(), props);
        Libp2pService svc = new Libp2pService(host, props, node, kad);

        String jwt = Jwts.builder()
                .setSubject("p2p")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor(props.getJwtSecret().getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
        Object handler = createTxHandler(svc);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        ByteBuf buf = buildMessage(jwt);
        var m = handler.getClass().getMethod("messageReceived", ChannelHandlerContext.class, ByteBuf.class);
        m.setAccessible(true);
        m.invoke(handler, ctx, buf);
        verify(ctx, never()).close();
        verify(node).acceptExternalTx(any());
    }
}
