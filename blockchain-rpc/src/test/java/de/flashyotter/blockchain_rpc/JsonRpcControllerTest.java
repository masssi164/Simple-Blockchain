package de.flashyotter.blockchain_rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = JsonRpcController.class)
@Import(RpcAutoConfiguration.class)
@ContextConfiguration(classes = JsonRpcController.class)
class JsonRpcControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    RpcHandler handler;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void dispatchesCall() throws Exception {
        when(handler.dispatch(eq("ping"), any())).thenReturn(RpcResponse.success("pong"));
        client.post()
                .uri("/rpc")
                .bodyValue(mapper.readTree("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"method\":\"ping\"," +
                        "\"params\":[]," +
                        "\"id\":1}"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("pong");
    }

    @Test
    void rejectsInvalidRequest() throws Exception {
        client.post()
                .uri("/rpc")
                .bodyValue(mapper.readTree("{\"jsonrpc\":\"2.0\"}"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.error.code").isEqualTo(-32600);
    }
}
