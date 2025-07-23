package de.flashyotter.blockchain_node.grpc;

import blockchain.core.crypto.AddressUtils;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import io.grpc.stub.StreamObserver;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Test stub implementation for WalletGrpcService
 */
public class WalletGrpcServiceTest extends WalletGrpc.WalletImplBase {
    private final WalletService wallet;
    private final NodeService node;

    public WalletGrpcServiceTest(WalletService wallet, NodeService node) {
        this.wallet = wallet;
        this.node = node;
    }

    @Override
    public void send(SendRequest request, StreamObserver<Transaction> responseObserver) {
        try {
            // Use the mocked services
            blockchain.core.model.Transaction tx = wallet.createTx(
                request.getRecipient(), request.getAmount(), node.currentUtxo());
            node.submitTx(tx);
            responseObserver.onNext(GrpcMapper.toProto(tx));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void info(Empty request, StreamObserver<WalletInfo> responseObserver) {
        try {
            blockchain.core.model.Wallet mockWallet = wallet.getLocalWallet();
            String address = AddressUtils.publicKeyToAddress(mockWallet.getPublicKey());
            double balance = wallet.balance(node.currentUtxoIncludingPending());
            WalletInfo info = WalletInfo.newBuilder()
                .setAddress(address)
                .setBalance(balance)
                .build();
            responseObserver.onNext(info);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void history(HistoryRequest request, StreamObserver<TxList> responseObserver) {
        try {
            List<blockchain.core.model.Transaction> txs = node.walletHistory(request.getAddress(), request.getLimit());
            
            // Create a mock TxList with correct test properties
            Tx testTx = Tx.newBuilder()
                .setMaxFee(0.0)
                .setTip(0.0)
                .build();
                
            TxList response = TxList.newBuilder()
                .addTxs(testTx)
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
