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

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.controler.WalletController;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean WalletService walletSvc;
    @MockitoBean NodeService   nodeSvc;
    @Autowired ObjectMapper    mapper;

    @Test @DisplayName("GET /api/wallet gives balance & address, using pending UTXO")
    void info() throws Exception {
        // prepare a fake public key and wallet
        String pkBase64 = "MFYw…fake…==";
        PublicKey pk = KeyFactory.getInstance("EC")
                         .generatePublic(new X509EncodedKeySpec(pkBase64.getBytes()));
        Wallet w = walletSvc.getLocalWallet();
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
}
