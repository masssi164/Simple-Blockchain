package de.flashyotter.blockchain_node.rpc;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_rpc.RpcResponse;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleRpcHandlerTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void ethChainIdReturnsHexQuantity() throws Exception {
        NodeService node = mock(NodeService.class);
        when(node.chainId()).thenReturn(1337L);
        SimpleRpcHandler handler = new SimpleRpcHandler(node);
        RpcResponse response = handler.dispatch("eth_chainId", mapper.readTree("[]"));
        assertEquals("0x539", response.result());
    }

    @Test
    void ethBlockNumberUsesLatestHeight() throws Exception {
        NodeService node = mock(NodeService.class);
        when(node.latestHeight()).thenReturn(12);
        SimpleRpcHandler handler = new SimpleRpcHandler(node);
        RpcResponse response = handler.dispatch("eth_blockNumber", mapper.readTree("[]"));
        assertEquals("0xc", response.result());
    }

    @Test
    void ethGetBalanceRespectsPendingFlag() throws Exception {
        String address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT";
        NodeService node = mock(NodeService.class);
        Map<String, TxOutput> confirmed = Map.of(
                "c1", new TxOutput(2.0, address)
        );
        Map<String, TxOutput> pending = Map.of(
                "c1", new TxOutput(2.0, address),
                "p1", new TxOutput(5.0, address)
        );
        when(node.currentUtxo()).thenReturn(confirmed);
        when(node.currentUtxoIncludingPending()).thenReturn(pending);

        SimpleRpcHandler handler = new SimpleRpcHandler(node);

        RpcResponse latest = handler.dispatch("eth_getBalance", mapper.readTree("[\"" + address + "\",\"latest\"]"));
        RpcResponse pendingRes = handler.dispatch("eth_getBalance", mapper.readTree("[\"" + address + "\",\"pending\"]"));

        assertEquals(Numeric.encodeQuantity(java.math.BigInteger.valueOf(200_000_000L)), latest.result());
        assertEquals(Numeric.encodeQuantity(java.math.BigInteger.valueOf(700_000_000L)), pendingRes.result());
    }

    @Test
    void ethSendRawTransactionAcceptsHexPayload() throws Exception {
        NodeService node = mock(NodeService.class);
        when(node.submitTx(any())).thenReturn(true);
        SimpleRpcHandler handler = new SimpleRpcHandler(node);

        String address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT";
        String json = "{" +
                "\"inputs\":[]," +
                "\"outputs\":[{" +
                "\"value\":1.0," +
                "\"recipientAddress\":\"" + address + "\"" +
                "}]," +
                "\"maxFee\":0.0," +
                "\"tip\":0.0" +
                "}";
        Transaction expectedTx = blockchain.core.serialization.JsonUtils.txFromJson(json);
        String hexPayload = Numeric.toHexString(json.getBytes(StandardCharsets.UTF_8));

        RpcResponse response = handler.dispatch(
                "eth_sendRawTransaction",
                mapper.readTree('[' + mapper.writeValueAsString(hexPayload) + ']')
        );

        assertTrue(response.result() instanceof String);
        org.mockito.ArgumentCaptor<Transaction> captor = org.mockito.ArgumentCaptor.forClass(Transaction.class);
        verify(node).submitTx(captor.capture());
        String expected = Numeric.prependHexPrefix(expectedTx.calcHashHex()).toLowerCase();
        String returned = ((String) response.result()).toLowerCase();
        assertEquals(expected, returned);
    }

    @Test
    void ethGetBlockByNumberWithTransactions() throws Exception {
        NodeService node = mock(NodeService.class);
        Transaction tx = new Transaction();
        tx.getOutputs().add(new TxOutput(1.0, "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"));
        Block block = new Block(5, "0".repeat(64), List.of(tx), 0x1f0fffff);
        when(node.blockAtHeight(5)).thenReturn(block);

        SimpleRpcHandler handler = new SimpleRpcHandler(node);
        RpcResponse response = handler.dispatch("eth_getBlockByNumber", mapper.readTree("[\"0x5\",true]"));

        assertNotNull(response.result());
        Map<?,?> blockMap = (Map<?,?>) response.result();
        assertEquals("0x5", blockMap.get("number"));
        assertEquals(Numeric.prependHexPrefix(block.getHashHex()), blockMap.get("hash"));
        List<?> txs = (List<?>) blockMap.get("transactions");
        assertEquals(1, txs.size());
        assertTrue(txs.get(0) instanceof Map);
        Map<?,?> txMap = (Map<?,?>) txs.get(0);
        assertEquals(Numeric.prependHexPrefix(tx.calcHashHex()), txMap.get("hash"));
    }

    @Test
    void legacySendTransactionStillReturnsHashField() throws Exception {
        NodeService node = mock(NodeService.class);
        when(node.submitTx(any())).thenReturn(true);
        SimpleRpcHandler handler = new SimpleRpcHandler(node);

        String address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT";
        String json = "{" +
                "\"inputs\":[]," +
                "\"outputs\":[{" +
                "\"value\":1.0," +
                "\"recipientAddress\":\"" + address + "\"" +
                "}]," +
                "\"maxFee\":0.0," +
                "\"tip\":0.0" +
                "}";

        RpcResponse response = handler.dispatch(
                "sendTransaction",
                mapper.readTree('[' + mapper.writeValueAsString(json) + ']')
        );

        assertTrue(response.result() instanceof Map<?,?>);
        org.mockito.ArgumentCaptor<Transaction> captor = org.mockito.ArgumentCaptor.forClass(Transaction.class);
        verify(node, atLeastOnce()).submitTx(captor.capture());
        assertEquals(captor.getValue().calcHashHex(), ((Map<?,?>) response.result()).get("hash"));
    }
}
