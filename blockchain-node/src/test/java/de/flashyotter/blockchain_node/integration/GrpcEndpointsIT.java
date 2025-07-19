package de.flashyotter.blockchain_node.integration;

import static org.assertj.core.api.Assertions.assertThat;

import de.flashyotter.blockchain_node.grpc.ChainGrpc;
import de.flashyotter.blockchain_node.grpc.Empty;
import de.flashyotter.blockchain_node.grpc.WalletGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = { "grpc.server.port=19090",
                   "node.jwt-secret=integration-secret-0123456789abcdef0123456789ab",
                   "node.data-path=build/test-data/grpc",
                   "node.libp2p-port=0" })
class GrpcEndpointsIT {

    private ManagedChannel channel;

    @AfterEach
    void shutdown() {
        if (channel != null) channel.shutdownNow();
    }

    @Test
    void walletInfoAccessible() {
        int port = 19090;
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        WalletGrpc.WalletBlockingStub stub = WalletGrpc.newBlockingStub(channel);
        var info = stub.info(Empty.newBuilder().build());
        assertThat(info.getAddress()).isNotBlank();
    }

    @Test
    void latestBlockAccessible() {
        int port = 19090;
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        ChainGrpc.ChainBlockingStub stub = ChainGrpc.newBlockingStub(channel);
        var blk = stub.latest(Empty.newBuilder().build());
        assertThat(blk.getHeight()).isGreaterThanOrEqualTo(0);
    }
}
