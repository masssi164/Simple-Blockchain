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

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import blockchain.core.model.Transaction;
import de.flashyotter.blockchain_node.controler.WalletController;
import de.flashyotter.blockchain_node.dto.SendFundsDto;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WalletService walletSvc;

    @MockitoBean
    private NodeService nodeSvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("GET /api/wallet returns wallet info")
    void info() throws Exception {
        String pk = "pubkey-bytes";
        double bal = 42.0;

        // stub: Wallet.getLocalWallet().getPublicKey()
        when(walletSvc.getLocalWallet()).thenReturn(
            new blockchain.core.model.Wallet() {
                @Override public java.security.PublicKey getPublicKey() {
                    try {
                        return java.security.KeyFactory
                              .getInstance("EC")
                              .generatePublic(new java.security.spec.X509EncodedKeySpec(pk.getBytes()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        );
        when(nodeSvc.currentUtxo()).thenReturn(Map.of());
        when(walletSvc.balance(anyMap())).thenReturn(bal);

        mvc.perform(get("/api/wallet"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.confirmedBalance").value(bal))
           .andExpect(jsonPath("$.publicKeyBase64").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/wallet/send sends funds")
    void send() throws Exception {
        SendFundsDto dto = new SendFundsDto("rEcIpIeNt", 1.23);
        Transaction tx = new Transaction();
        when(walletSvc.createTx(eq(dto.recipient()), eq(dto.amount()), anyMap()))
            .thenReturn(tx);

        mvc.perform(post("/api/wallet/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
           .andExpect(status().isOk())
           .andExpect(content().json(mapper.writeValueAsString(tx)));

        verify(nodeSvc, times(1)).submitTx(tx);
    }
}
