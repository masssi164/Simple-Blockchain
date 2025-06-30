package de.flashyotter.blockchain_node.controller;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PublicKey;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.controller.WalletController;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import java.util.List;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired MockMvc mvc;
    @MockBean WalletService walletSvc;
    @MockBean NodeService   nodeSvc;
    @Autowired ObjectMapper    mapper;

    @Test @DisplayName("GET /api/wallet gives balance & address, using pending UTXO")
    void info() throws Exception {
        // prepare a fake public key and wallet
        // Use a generated wallet for key material
        Wallet w = new Wallet();
        PublicKey pk = w.getPublicKey();
        when(walletSvc.getLocalWallet()).thenReturn(w);
        when(nodeSvc.currentUtxoIncludingPending()).thenReturn(Map.of());
        when(walletSvc.balance(anyMap())).thenReturn(42.0);

        // perform request
        mvc.perform(get("/api/wallet"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.confirmedBalance").value(42.0))
           .andExpect(jsonPath("$.address").value(
               AddressUtils.publicKeyToAddress(pk)))
           ;

        // verify that we used the new UTXO method
        verify(nodeSvc, times(1)).currentUtxoIncludingPending();
    }

    @Test @DisplayName("POST /api/wallet/send routes tx")
    void send() throws Exception {
        SendFundsDto dto = new SendFundsDto("addr1", 1.0);
        Transaction  tx  = new Transaction();
        when(walletSvc.createTx(eq("addr1"), eq(1.0), anyMap())).thenReturn(tx);

        mvc.perform(post("/api/wallet/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
           .andExpect(status().isOk())
           .andExpect(content().json(mapper.writeValueAsString(tx)));

        verify(nodeSvc).submitTx(tx);
    }

    @Test
    void historyEndpoint() throws Exception {
        Transaction tx = new Transaction();
        when(nodeSvc.walletHistory("addr1", 5)).thenReturn(List.of(tx));

        mvc.perform(get("/api/wallet/transactions")
                .param("address", "addr1")
                .param("limit", "5"))
           .andExpect(status().isOk())
           .andExpect(content().json(mapper.writeValueAsString(List.of(tx))));

        verify(nodeSvc).walletHistory("addr1", 5);
    }
}
