package de.flashyotter.blockchain_node.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.flashyotter.blockchain_node.config.NodeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

@WebMvcTest(SnapshotController.class)
@AutoConfigureMockMvc(addFilters = false)
class SnapshotControllerTest {

    @Autowired MockMvc mvc;
    @MockBean NodeProperties props;

    @TempDir Path temp;

    @BeforeEach
    void setup() throws Exception {
        java.nio.file.Path dir = temp.resolve("snapshots");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("0000001.json.gz"), "dummy");
        org.mockito.Mockito.when(props.getDataPath()).thenReturn(temp.toString());
    }

    @Test
    void returnsSnapshotWhenExists() throws Exception {
        mvc.perform(get("/snap/1"))
           .andExpect(status().isOk());
    }

    @Test
    void notFoundForMissing() throws Exception {
        mvc.perform(get("/snap/99"))
           .andExpect(status().isNotFound());
    }
}
