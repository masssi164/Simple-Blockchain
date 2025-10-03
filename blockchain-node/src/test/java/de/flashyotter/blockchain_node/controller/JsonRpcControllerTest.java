package de.flashyotter.blockchain_node.controller;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JsonRpcController.class)
@AutoConfigureMockMvc(addFilters = false)
class JsonRpcControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NodeService node;

    @MockBean
    private WalletService wallet;

    private Wallet localWallet;
    private String localAddress;

    @BeforeEach
    void setUp() {
        localWallet = new Wallet();
        localAddress = AddressUtils.publicKeyToAddress(localWallet.getPublicKey());
        when(wallet.getLocalWallet()).thenReturn(localWallet);
    }

    @Test
    void sbChainLatestReturnsBlockView() throws Exception {
        Block block = new Block(3, "00", List.of(), 0x1f0fffff);
        when(node.latestBlock()).thenReturn(block);

        mvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"id\":1," +
                        "\"method\":\"sb_chainLatest\"" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(3))
                .andExpect(jsonPath("$.result.hashHex").value(block.getHashHex()));
    }

    @Test
    void ethSendTransactionReturnsHash() throws Exception {
        Transaction tx = new Transaction();
        tx.getOutputs().add(new TxOutput(1.0, localAddress));
        when(wallet.createTx(eq(localAddress), eq(1.0), anyMap())).thenReturn(tx);
        when(node.currentUtxo()).thenReturn(Map.of());
        when(node.submitTx(tx)).thenReturn(true);

        mvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"id\":7," +
                        "\"method\":\"eth_sendTransaction\"," +
                        "\"params\":[{" +
                        "\"to\":\"" + localAddress + "\"," +
                        "\"value\":\"0x5f5e100\"" +
                        "}]" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0x" + tx.calcHashHex()));
    }

    @Test
    void walletInfoExposesPendingBalances() throws Exception {
        Map<String, TxOutput> confirmed = Map.of(
                "a", new TxOutput(5.0, localAddress)
        );
        Map<String, TxOutput> effective = Map.of(
                "a", new TxOutput(5.0, localAddress),
                "b", new TxOutput(1.0, localAddress)
        );
        when(node.currentUtxo()).thenReturn(confirmed);
        when(node.currentUtxoIncludingPending()).thenReturn(effective);
        when(wallet.balance(confirmed)).thenReturn(5.0);
        when(wallet.balance(effective)).thenReturn(6.0);

        mvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"jsonrpc\":\"2.0\"," +
                        "\"id\":9," +
                        "\"method\":\"sb_walletInfo\"" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.address").value(localAddress))
                .andExpect(jsonPath("$.result.confirmedBalance").value(5.0))
                .andExpect(jsonPath("$.result.pendingIncoming").value(1.0))
                .andExpect(jsonPath("$.result.pendingOutgoing").value(0.0));
    }
}
