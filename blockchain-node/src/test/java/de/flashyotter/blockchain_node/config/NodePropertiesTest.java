package de.flashyotter.blockchain_node.config;

import de.flashyotter.blockchain_node.service.PublicIpService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NodePropertiesTest {

    @Test
    void initAddressUsesService() {
        NodeProperties props = new NodeProperties();
        PublicIpService svc = mock(PublicIpService.class);
        when(svc.fetchPublicIp()).thenReturn("203.0.113.9");
        props.setIpService(svc);

        props.initAddress();

        assertEquals("203.0.113.9", props.getPublicHost());
        verify(svc).fetchPublicIp();
    }
}
