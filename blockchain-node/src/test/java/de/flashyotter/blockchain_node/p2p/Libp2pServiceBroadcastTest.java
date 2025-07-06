package de.flashyotter.blockchain_node.p2p;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.serialization.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.dto.NewTxDto;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
import de.flashyotter.blockchain_node.service.KademliaService;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.service.PeerRegistry;
import io.libp2p.core.Host;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.mux.StreamMuxerProtocol;
import io.libp2p.crypto.keys.Secp256k1Kt;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.transport.tcp.TcpTransport;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Disabled("Requires libp2p hosts to connect")
class Libp2pServiceBroadcastTest {

    Host h1, h2;
    Libp2pService s1, s2;
    NodeService n1, n2;
    NodeProperties props1, props2;

    static int freePort() throws Exception {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    static Host makeHost(int port) {
        io.libp2p.core.multiformats.Multiaddr addr =
                new io.libp2p.core.multiformats.Multiaddr("/ip4/127.0.0.1/tcp/" + port);
        Host host = new HostBuilder()
                .builderModifier(b -> b.getIdentity().setFactory(() -> Secp256k1Kt.generateSecp256k1KeyPair().component1()))
                .transport(TcpTransport::new)
                .secureChannel(NoiseXXSecureChannel::new)
                .muxer(StreamMuxerProtocol::getYamux)
                .listen(addr.toString())
                .build();
        host.start().join();
        return host;
    }

    @BeforeEach
    void setup() throws Exception {
        int p1 = freePort();
        int p2 = freePort();

        props1 = new NodeProperties();
        props1.setLibp2pPort(p1);
        props1.setId("n1");
        props2 = new NodeProperties();
        props2.setLibp2pPort(p2);
        props2.setId("n2");

        h1 = makeHost(p1);
        h2 = makeHost(p2);

        n1 = mock(NodeService.class);
        n2 = mock(NodeService.class);

        PeerRegistry r1 = new PeerRegistry();
        PeerRegistry r2 = new PeerRegistry();
        KademliaRoutingTable<Peer> t1 = KademliaRoutingTable.create(
                props1.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        KademliaRoutingTable<Peer> t2 = KademliaRoutingTable.create(
                props2.getId().getBytes(StandardCharsets.UTF_8), 16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8), p -> 0);
        KademliaService k1 = new KademliaService(t1, r1, props1);
        KademliaService k2 = new KademliaService(t2, r2, props2);

        s1 = new Libp2pService(h1, props1, new ObjectMapper(), n1, k1);
        s2 = new Libp2pService(h2, props2, new ObjectMapper(), n2, k2);
        s1.init();
        s2.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        h1.stop().join();
        h2.stop().join();
    }

    @Test
    void broadcastTransaction() {
        Peer p2 = new Peer("127.0.0.1", props2.getLibp2pPort(), h2.getPeerId().toBase58());
        Transaction tx = new Transaction();
        tx.getOutputs().add(new TxOutput(1.0, "addr"));
        NewTxDto dto = new NewTxDto(JsonUtils.toJson(tx));
        s1.broadcastTxs(List.of(p2), dto);

        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(n2).acceptExternalTx(any()));
    }

    @Test
    void broadcastBlock() {
        Peer p2 = new Peer("127.0.0.1", props2.getLibp2pPort(), h2.getPeerId().toBase58());
        Transaction coin = new Transaction();
        coin.getOutputs().add(new TxOutput(50.0, "miner"));
        blockchain.core.model.Block blk = new blockchain.core.model.Block(1, "prev", List.of(coin), 1);
        de.flashyotter.blockchain_node.dto.NewBlockDto dto = new de.flashyotter.blockchain_node.dto.NewBlockDto(blockchain.core.serialization.JsonUtils.toJson(blk));
        s1.broadcastBlocks(List.of(p2), dto);
        Awaitility.await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(n2).acceptExternalBlock(any()));
    }
}
