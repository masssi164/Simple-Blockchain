package de.flashyotter.blockchain_node.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JsonRpcController.class)
@AutoConfigureMockMvc(addFilters = false)
class JsonRpcControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NodeService node;

    @MockBean
    private NodeProperties props;

    @BeforeEach
    void setUp() {
        when(props.getMinerAddress()).thenReturn("abcd");
    }

    @Test
    void sbChainLatestReturnsBlockView() throws Exception {
        Block block = new Block(3, "00", List.of(), 0x1f0fffff);
        when(node.latestBlock()).thenReturn(block);

        mvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"id\":1," +
                        "\"method\":\"sb_chainLatest\"" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(3))
                .andExpect(jsonPath("$.result.hashHex").value(block.getHashHex()));
    }

    @Test
    void ethGetBalanceSumsOutputs() throws Exception {
        Map<String, TxOutput> utxo = Map.of(
                "one", new TxOutput(1.0, "abcd"),
                "two", new TxOutput(0.5, "abcd"),
                "other", new TxOutput(2.0, "zzzz")
        );
        when(node.currentUtxoIncludingPending()).thenReturn(utxo);

        mvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"id\":5," +
                        "\"method\":\"eth_getBalance\"," +
                        "\"params\":[\"abcd\"]" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0x8f0d180"));
    }

    @Test
    void ethGetBlockByNumberUsesConfiguredMiner() throws Exception {
        Transaction coinbase = new Transaction();
        coinbase.getOutputs().add(new TxOutput(1.0, "miner-address"));
        Block block = new Block(5, "00", List.of(coinbase), 0x1f0fffff, 1L, 0);
        when(node.blockAtHeight(anyInt())).thenReturn(block);

        mvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"id\":6," +
                        "\"method\":\"eth_getBlockByNumber\"," +
                        "\"params\":[\"0x5\",true]" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.miner").value("0xabcd"));
    }
}
