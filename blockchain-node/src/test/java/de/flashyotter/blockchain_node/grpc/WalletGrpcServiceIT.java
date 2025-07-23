package de.flashyotter.blockchain_node.grpc;

import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import blockchain.core.crypto.AddressUtils;
// Using our own Empty class
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletGrpcServiceIT {

    @Mock
    private WalletService walletService;

    @Mock
    private NodeService nodeService;

    private WalletGrpcServiceTest service;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeEach
    void setUp() {
        service = new WalletGrpcServiceTest(walletService, nodeService);
    }

    @Test
    void testGetWalletInfo() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // Arrange
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
        kpg.initialize(new ECGenParameterSpec("secp256k1"));
        KeyPair kp = kpg.generateKeyPair();
        Wallet wallet = new Wallet(kp.getPrivate(), kp.getPublic());
        
        String address = AddressUtils.publicKeyToAddress(kp.getPublic());
        when(walletService.getLocalWallet()).thenReturn(wallet);
        Map<String, TxOutput> utxo = new HashMap<>();
        when(nodeService.currentUtxoIncludingPending()).thenReturn(utxo);

        @SuppressWarnings("unchecked")
        StreamObserver<WalletInfo> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<WalletInfo> walletInfoCaptor = ArgumentCaptor.forClass(WalletInfo.class);

        // Act
        service.info(Empty.getDefaultInstance(), responseObserver);

        // Assert
        verify(responseObserver).onNext(walletInfoCaptor.capture());
        verify(responseObserver).onCompleted();

        WalletInfo walletInfo = walletInfoCaptor.getValue();
        assertThat(walletInfo).isNotNull();
        assertThat(walletInfo.getBalance()).isEqualTo(0);
        assertThat(walletInfo.getAddress()).isEqualTo(address);
    }

    @Test
    void testSendTransaction() {
        // Arrange
        String recipient = "0x456def";
        double amount = 100.0;

        Map<String, TxOutput> utxo = new HashMap<>();
        Transaction mockTx = new Transaction();
        mockTx.setMaxFee(0.0);
        mockTx.setTip(0.0);

        when(nodeService.currentUtxo()).thenReturn(utxo);
        when(walletService.createTx(anyString(), anyDouble(), any())).thenReturn(mockTx);

        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Transaction> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<de.flashyotter.blockchain_node.grpc.Transaction> txCaptor = 
            ArgumentCaptor.forClass(de.flashyotter.blockchain_node.grpc.Transaction.class);

        // Act
        service.send(SendRequest.newBuilder()
            .setRecipient(recipient)
            .setAmount(amount)
            .build(), responseObserver);

        // Assert
        verify(nodeService).submitTx(mockTx);
        verify(responseObserver).onNext(txCaptor.capture());
        verify(responseObserver).onCompleted();

        de.flashyotter.blockchain_node.grpc.Transaction responseTx = txCaptor.getValue();
        assertThat(responseTx).isNotNull();
        assertThat(responseTx.getMaxFee()).isEqualTo(0.0);
        assertThat(responseTx.getTip()).isEqualTo(0.0);
    }

    @Test
    void testGetHistory() {
        // Arrange
        String address = "0x123abc";
        Transaction mockTx = new Transaction();
        mockTx.setMaxFee(0.0);
        mockTx.setTip(0.0);

        when(nodeService.walletHistory(anyString(), anyInt())).thenReturn(Collections.singletonList(mockTx));

        @SuppressWarnings("unchecked")
        StreamObserver<TxList> responseObserver = mock(StreamObserver.class);
        ArgumentCaptor<TxList> txListCaptor = ArgumentCaptor.forClass(TxList.class);

        // Act
        service.history(HistoryRequest.newBuilder()
            .setAddress(address)
            .setLimit(10)
            .build(), responseObserver);

        // Assert
        verify(responseObserver).onNext(txListCaptor.capture());
        verify(responseObserver).onCompleted();

        TxList txList = txListCaptor.getValue();
        assertThat(txList).isNotNull();
        assertThat(txList.getTxsCount()).isEqualTo(1);
        assertThat(txList.getTxs(0).getMaxFee()).isEqualTo(0.0);
        assertThat(txList.getTxs(0).getTip()).isEqualTo(0.0);
    }

    @Test
    void testSend_Error() {
        // Arrange
        String recipient = "0x456def";
        double amount = 100.0;

        Map<String, TxOutput> utxo = new HashMap<>();
        when(nodeService.currentUtxo()).thenReturn(utxo);
        when(walletService.createTx(anyString(), anyDouble(), any())).thenThrow(new RuntimeException("test error"));

        @SuppressWarnings("unchecked")
        StreamObserver<de.flashyotter.blockchain_node.grpc.Transaction> responseObserver = mock(StreamObserver.class);

        // Act
        service.send(SendRequest.newBuilder()
            .setRecipient(recipient)
            .setAmount(amount)
            .build(), responseObserver);

        // Assert
        verify(responseObserver).onError(any(RuntimeException.class));
    }

    @Test
    void testGetHistory_Error() {
        // Arrange
        String address = "0x123abc";
        when(nodeService.walletHistory(anyString(), anyInt())).thenThrow(new RuntimeException("test error"));

        @SuppressWarnings("unchecked")
        StreamObserver<TxList> responseObserver = mock(StreamObserver.class);

        // Act
        service.history(HistoryRequest.newBuilder()
            .setAddress(address)
            .setLimit(10)
            .build(), responseObserver);

        // Assert
        verify(responseObserver).onError(any(RuntimeException.class));
    }
}
