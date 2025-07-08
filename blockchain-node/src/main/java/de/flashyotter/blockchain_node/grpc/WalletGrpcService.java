package de.flashyotter.blockchain_node.grpc;

import blockchain.core.crypto.AddressUtils;
import de.flashyotter.blockchain_node.service.NodeService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class WalletGrpcService extends WalletGrpc.WalletImplBase {

    private final WalletService wallet;
    private final NodeService node;

    public WalletGrpcService(WalletService wallet, NodeService node) {
        this.wallet = wallet;
        this.node = node;
    }

    @Override
    public void send(SendRequest request, StreamObserver<Transaction> responseObserver) {
        var tx = wallet.createTx(request.getRecipient(), request.getAmount(), node.currentUtxo());
        node.submitTx(tx);
        responseObserver.onNext(GrpcMapper.toProto(tx));
        responseObserver.onCompleted();
    }

    @Override
    public void info(Empty request, StreamObserver<WalletInfo> responseObserver) {
        var utxo = node.currentUtxoIncludingPending();
        double balance = wallet.balance(utxo);
        String address = AddressUtils.publicKeyToAddress(wallet.getLocalWallet().getPublicKey());
        var info = WalletInfo.newBuilder().setAddress(address).setBalance(balance).build();
        responseObserver.onNext(info);
        responseObserver.onCompleted();
    }

    @Override
    public void history(HistoryRequest request, StreamObserver<TxList> responseObserver) {
        var txs = node.walletHistory(request.getAddress(), request.getLimit());
        var list = TxList.newBuilder()
                .addAllTxs(txs.stream().map(GrpcMapper::toProto).toList())
                .build();
        responseObserver.onNext(list);
        responseObserver.onCompleted();
    }
}
