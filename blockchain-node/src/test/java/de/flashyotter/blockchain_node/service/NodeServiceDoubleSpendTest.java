package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import blockchain.core.consensus.Chain;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.storage.InMemoryBlockStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/** Verifies that double-spends are rejected across pending transactions. */
class NodeServiceDoubleSpendTest {

    @Test
    void doubleSpendIsRejected() {
        // create a chain with one spendable output to alice
        Chain chain = new Chain();
        Wallet alice = new Wallet();
        Transaction coinbase = new Transaction(alice.getPublicKey(), 10.0, "cb");
        Block blk = new Block(
                1,
                chain.getLatest().getHashHex(),
                List.of(coinbase),
                chain.getLatest().getCompactDifficultyBits()
        );
        blk.mineLocally();
        chain.addBlock(blk);

        MempoolService mempool = new MempoolService();
        NodeService svc = new NodeService(
                chain,
                mempool,
                Mockito.mock(MiningService.class),
                Mockito.mock(P2PBroadcastService.class),
                new InMemoryBlockStore()
        );

        // spend the coinbase output once
        String utxoId = coinbase.getOutputs().get(0).id(coinbase.calcHashHex(), 0);
        Transaction tx1 = new Transaction();
        tx1.getInputs().add(new TxInput(utxoId, new byte[0], alice.getPublicKey()));
        tx1.getOutputs().add(new TxOutput(10.0, alice.getPublicKey()));
        tx1.signInputs(alice.getPrivateKey());
        svc.submitTx(tx1);

        // second spend of the same output must fail
        Transaction tx2 = new Transaction();
        tx2.getInputs().add(new TxInput(utxoId, new byte[0], alice.getPublicKey()));
        tx2.getOutputs().add(new TxOutput(10.0, alice.getPublicKey()));
        tx2.signInputs(alice.getPrivateKey());

        assertThrows(BlockchainException.class, () -> svc.submitTx(tx2));
    }
}
