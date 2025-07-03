package de.flashyotter.blockchain_node.p2p;

import de.flashyotter.blockchain_node.config.NodeProperties;
import io.libp2p.core.Host;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.crypto.keys.Secp256k1Kt;
import io.libp2p.core.mux.StreamMuxerProtocol;
import io.libp2p.discovery.MDnsDiscovery;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.transport.tcp.TcpTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Libp2pConfig {

    @Bean(destroyMethod = "stop")
    public Host libp2pHost(NodeProperties props) {
        int listenPort = 4001; // read from application.yml
        Multiaddr addr = new Multiaddr("/ip4/0.0.0.0/tcp/" + listenPort);
        Host host = new HostBuilder()
                .builderModifier(b -> b.getIdentity().setFactory(() ->
                        Secp256k1Kt.generateSecp256k1KeyPair().component1()))
                .transport(TcpTransport::new)
                .secureChannel(NoiseXXSecureChannel::new)
                .muxer(StreamMuxerProtocol::getYamux)
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
}
