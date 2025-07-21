package de.flashyotter.blockchain_node.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.p2p.libp2p.Libp2pService;
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
    @MockBean Libp2pService libp2p;

    @Test
    void exposesId() throws Exception {
        when(props.getId()).thenReturn("node-123");
        mvc.perform(get("/node/id"))
           .andExpect(status().isOk())
           .andExpect(content().json("{\"nodeId\":\"node-123\"}"));
    }

    @Test
    void exposesPeerId() throws Exception {
        when(libp2p.peerId()).thenReturn("pid-abc");
        mvc.perform(get("/node/peer-id"))
           .andExpect(status().isOk())
           .andExpect(content().json("{\"peerId\":\"pid-abc\"}"));
    }

    @Test
    void exposesEnr() throws Exception {
        when(libp2p.enr()).thenReturn("/ip4/127.0.0.1/tcp/1/p2p/abc");
        mvc.perform(get("/node/enr"))
           .andExpect(status().isOk())
           .andExpect(content().json("{\"enr\":\"/ip4/127.0.0.1/tcp/1/p2p/abc\"}"));
    }
}
