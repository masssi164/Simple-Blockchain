package de.flashyotter.blockchain_node.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import blockchain.core.model.Wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.controller.ChainController;
import de.flashyotter.blockchain_node.service.NodeService;

@WebMvcTest(ChainController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChainControllerTest {

    @Autowired MockMvc mvc;
    @MockBean NodeService nodeSvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void latest() throws Exception {
        Block tip = new Block(0, "0".repeat(64),
            List.of(new blockchain.core.model.Transaction(new Wallet().getPublicKey(), 0)), 0);
        when(nodeSvc.latestBlock()).thenReturn(tip);

        mvc.perform(get("/api/chain/latest"))
           .andExpect(status().isOk())
           .andExpect(content().json(mapper.writeValueAsString(tip)));

        verify(nodeSvc, times(1)).latestBlock();
    }

    @Test
    void blocksFromHeight() throws Exception {
        Block b0 = new Block(0, "0".repeat(64),
            List.of(new blockchain.core.model.Transaction(new Wallet().getPublicKey(), 0)), 0);
        List<Block> list = List.of(b0);
        when(nodeSvc.blocksFromHeight(5)).thenReturn(list);

        mvc.perform(get("/api/chain").param("from", "5"))
           .andExpect(status().isOk())
           .andExpect(content().json(mapper.writeValueAsString(list)));

        verify(nodeSvc, times(1)).blocksFromHeight(5);
    }

    @Test
    void blockPage() throws Exception {
        Block b0 = new Block(0, "0".repeat(64),
            List.of(new blockchain.core.model.Transaction(new Wallet().getPublicKey(), 0)), 0);
        List<Block> list = List.of(b0);
        when(nodeSvc.blockPage(1, 5)).thenReturn(list);

        mvc.perform(get("/api/chain/page").param("page", "1").param("size", "5"))
           .andExpect(status().isOk())
           .andExpect(content().json(mapper.writeValueAsString(list)));

        verify(nodeSvc, times(1)).blockPage(1, 5);
    }
}
