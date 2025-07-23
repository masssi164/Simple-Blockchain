package de.flashyotter.blockchain_node.config;

import io.grpc.*;
import de.flashyotter.blockchain_node.config.JwtUtils;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * gRPC interceptor validating the Authorization Bearer JWT header.
 */
@Component
@GrpcGlobalServerInterceptor
public class JwtGrpcInterceptor implements ServerInterceptor {

    private final NodeProperties props;

    public JwtGrpcInterceptor(NodeProperties props) {
        this.props = props;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                  Metadata headers,
                                                                  ServerCallHandler<ReqT, RespT> next) {
        String auth = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (JwtUtils.verify(token, props)) {
                return next.startCall(call, headers);
            } else {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), new Metadata());
                return new ServerCall.Listener<>() {};
            }
        }
        call.close(Status.UNAUTHENTICATED.withDescription("Missing token"), new Metadata());
        return new ServerCall.Listener<>() {};
    }
}
