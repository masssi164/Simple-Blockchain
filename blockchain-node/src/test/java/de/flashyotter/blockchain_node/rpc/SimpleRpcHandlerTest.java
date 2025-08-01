package de.flashyotter.blockchain_node.rpc;

import blockchain.core.model.TxOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.service.NodeService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleRpcHandlerTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void getBalance() throws Exception {
        NodeService node = mock(NodeService.class);
        when(node.currentUtxoIncludingPending()).thenReturn(Map.of(
                "o1", new TxOutput(5.0, "addr")
        ));
        SimpleRpcHandler handler = new SimpleRpcHandler(node);
        Object result = handler.dispatch("getBalance", mapper.readTree("[\"addr\"]"));
        assertEquals(5.0, ((Map<?,?>)result).get("balance"));
    }

    @Test
    void missingBlockReturnsError() throws Exception {
        NodeService node = mock(NodeService.class);
        when(node.blocksFromHeight(anyInt())).thenReturn(java.util.List.of());
        SimpleRpcHandler handler = new SimpleRpcHandler(node);
        Object result = handler.dispatch("getBlockByNumber", mapper.readTree("[1]"));
        assertEquals("Block not found", ((Map<?,?>)result).get("error"));
    }

    @Test
    void invalidTransactionReturnsError() throws Exception {
        NodeService node = mock(NodeService.class);
        SimpleRpcHandler handler = new SimpleRpcHandler(node);
        Object result = handler.dispatch("sendTransaction", mapper.readTree("[\"invalid\"]"));
        assertEquals("Invalid transaction format", ((Map<?,?>)result).get("error"));
        verify(node, never()).submitTx(any());
    }
}
