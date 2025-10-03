package de.flashyotter.blockchain_node.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.service.NodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UtxoController.class)
@AutoConfigureMockMvc(addFilters = false)
class UtxoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NodeService node;

    @Test
    void returnsMatchingUtxosOnly() throws Exception {
        Map<String, TxOutput> utxo = Map.of(
                "a:0", new TxOutput(1.25, "addr1"),
                "b:0", new TxOutput(2.0, "other"),
                "c:1", new TxOutput(0.5, "addr1")
        );
        when(node.currentUtxoIncludingPending()).thenReturn(utxo);

        mvc.perform(get("/api/utxo")
                .param("address", "addr1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("a:0"))
                .andExpect(jsonPath("$[0].value").value(1.25))
                .andExpect(jsonPath("$[0].recipientAddress").value("addr1"))
                .andExpect(jsonPath("$[1].id").value("c:1"))
                .andExpect(jsonPath("$[1].value").value(0.5))
                .andExpect(jsonPath("$[1].recipientAddress").value("addr1"))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
    }
}
