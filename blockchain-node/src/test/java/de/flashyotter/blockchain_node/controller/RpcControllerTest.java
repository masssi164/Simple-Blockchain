package de.flashyotter.blockchain_node.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.controler.RpcController;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import reactor.core.publisher.Mono;

@WebMvcTest(RpcController.class)
@AutoConfigureMockMvc(addFilters = false)
class RpcControllerTest {

    @Autowired MockMvc mvc;
    @MockBean NodeService nodeSvc;
    @MockBean WalletService walletSvc;
    @Autowired ObjectMapper mapper;

    @Test
    void latestBlockMethod() throws Exception {
        Block b = new Block(0, "0".repeat(64),
            List.of(new Transaction(new Wallet().getPublicKey(), 0)), 0);
        when(nodeSvc.latestBlock()).thenReturn(b);

        mvc.perform(post("/api/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jsonrpc\":\"2.0\",\"method\":\"chain_latestBlock\",\"id\":\"1\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.result.height").value(0));

        verify(nodeSvc).latestBlock();
    }

    @Test
    void methodNotFound() throws Exception {
        mvc.perform(post("/api/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jsonrpc\":\"2.0\",\"method\":\"unknown\",\"id\":\"1\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.error.code").value(-32601));
    }
}

