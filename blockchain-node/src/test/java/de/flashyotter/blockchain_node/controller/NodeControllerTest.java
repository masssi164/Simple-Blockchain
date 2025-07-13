package de.flashyotter.blockchain_node.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.flashyotter.blockchain_node.config.NodeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NodeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NodeControllerTest {

    @Autowired MockMvc mvc;
    @MockBean NodeProperties props;

    @Test
    void exposesId() throws Exception {
        when(props.getId()).thenReturn("node-123");
        mvc.perform(get("/node/id"))
           .andExpect(status().isOk())
           .andExpect(content().json("{\"nodeId\":\"node-123\"}"));
    }
}
