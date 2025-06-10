package de.flashyotter.blockchain_node.controller;

import static blockchain.core.serialization.JsonUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.controler.TxController;
import de.flashyotter.blockchain_node.service.NodeService;

@WebMvcTest(TxController.class)
@AutoConfigureMockMvc(addFilters = false)
class TxControllerTest {

    @Autowired MockMvc mvc;
    @MockBean NodeService nodeSvc;

    @Test
    void submit() throws Exception {
        Transaction tx = new Transaction();
        mvc.perform(post("/api/tx")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(tx)))
           .andExpect(status().isAccepted())
           .andExpect(content().json(toJson(tx)));

        verify(nodeSvc, times(1)).submitTx(any(Transaction.class));
    }
}
