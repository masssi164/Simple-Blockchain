package de.flashyotter.blockchain_node.p2p;

import de.flashyotter.blockchain_node.config.NodeProperties;
import io.libp2p.core.Host;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.crypto.keys.Secp256k1Kt;
import io.libp2p.core.mux.StreamMuxerProtocol;
import io.libp2p.discovery.MDnsDiscovery;
import io.libp2p.protocol.autonat.AutonatProtocol;
import io.libp2p.protocol.autonat.AutonatProtocol.Binding;
import org.apache.tuweni.kademlia.KademliaRoutingTable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import de.flashyotter.blockchain_node.p2p.Peer;
import de.flashyotter.blockchain_node.service.PeerStore;
import de.flashyotter.blockchain_node.service.TablePeerStore;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.security.plaintext.PlaintextInsecureChannel;
import io.libp2p.transport.tcp.TcpTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Libp2pConfig {

    @Bean(destroyMethod = "stop")
    public Host libp2pHost(NodeProperties props) {
        int listenPort = props.getLibp2pPort();
        Multiaddr addr = new Multiaddr("/ip4/0.0.0.0/tcp/" + listenPort);
        java.util.function.BiFunction<io.libp2p.core.crypto.PrivKey,
                java.util.List<io.libp2p.core.mux.StreamMuxer>,
                io.libp2p.core.security.SecureChannel> secureFactory =
                props.isLibp2pEncrypted() ? NoiseXXSecureChannel::new : PlaintextInsecureChannel::new;

        Path keyFile = java.nio.file.Path.of(props.getLibp2pKeyPath());
        io.libp2p.core.crypto.PrivKey priv;
        try {
            if (java.nio.file.Files.exists(keyFile)) {
                byte[] raw = java.nio.file.Files.readAllBytes(keyFile);
                priv = Secp256k1Kt.unmarshalSecp256k1PrivateKey(raw);
            } else {
                var pair = Secp256k1Kt.generateSecp256k1KeyPair();
                priv = pair.component1();
                Path dir = keyFile.getParent();
                if (dir != null) java.nio.file.Files.createDirectories(dir);
                java.nio.file.Files.write(keyFile, priv.bytes());
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load libp2p key", e);
        }

        Host host = new HostBuilder()
                .builderModifier(b -> b.getIdentity().setFactory(() -> priv))
                .transport(TcpTransport::new)
                .secureChannel(secureFactory)
                .muxer(StreamMuxerProtocol::getYamux)
                .protocol(new Binding())
                .listen(addr.toString())
                .build();
        host.start().join();
        try {
            new MDnsDiscovery(
                    host,
                    MDnsDiscovery.Companion.getServiceTagLocal(),
                    MDnsDiscovery.Companion.getQueryInterval(),
                    java.net.InetAddress.getLocalHost()).start();
        } catch (java.net.UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return host;
    }

    @Bean
    public KademliaRoutingTable<Peer> kademliaRouting(NodeProperties props) {
        return KademliaRoutingTable.create(
                props.getId().getBytes(StandardCharsets.UTF_8),
                16,
                p -> p.toString().getBytes(StandardCharsets.UTF_8),
                p -> 0);
    }

    @Bean
    public PeerStore peerStore(KademliaRoutingTable<Peer> table) {
        return new TablePeerStore(table);
    }
}
