package de.flashyotter.blockchain_node.grpc;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import com.google.protobuf.ByteString;

/** Utility converting domain objects to their gRPC representations. */
public final class GrpcMapper {
    private GrpcMapper() {}

    public static de.flashyotter.blockchain_node.grpc.TxInput toProto(TxInput in) {
        return de.flashyotter.blockchain_node.grpc.TxInput.newBuilder()
                .setReferencedOutputId(in.getReferencedOutputId())
                .setSignature(ByteString.copyFrom(in.getSignature()))
                .setSender(ByteString.copyFrom(in.getSender().getEncoded()))
                .build();
    }

    public static de.flashyotter.blockchain_node.grpc.TxOutput toProto(TxOutput out) {
        return de.flashyotter.blockchain_node.grpc.TxOutput.newBuilder()
                .setValue(out.value())
                .setRecipientAddress(out.recipientAddress())
                .build();
    }

    public static de.flashyotter.blockchain_node.grpc.Transaction toProto(Transaction tx) {
        var b = de.flashyotter.blockchain_node.grpc.Transaction.newBuilder()
                .addAllInputs(tx.getInputs().stream().map(GrpcMapper::toProto).toList())
                .addAllOutputs(tx.getOutputs().stream().map(GrpcMapper::toProto).toList())
                .setMaxFee(tx.getMaxFee())
                .setTip(tx.getTip());
        return b.build();
    }

    public static de.flashyotter.blockchain_node.grpc.Block toProto(Block block) {
        var b = de.flashyotter.blockchain_node.grpc.Block.newBuilder()
                .setHeight(block.getHeight())
                .setPreviousHashHex(block.getPreviousHashHex())
                .setTimeMillis(block.getTimeMillis())
                .setCompactBits(block.getCompactDifficultyBits())
                .setNonce(block.getNonce())
                .setMerkleRootHex(block.getMerkleRootHex())
                .addAllTxList(block.getTxList().stream().map(GrpcMapper::toProto).toList());
        return b.build();
    }
}
