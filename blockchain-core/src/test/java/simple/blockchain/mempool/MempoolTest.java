package simple.blockchain.mempool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.exceptions.BlockchainException;
import blockchain.core.mempool.Mempool;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/**
 * Fast validation tests for {@link Mempool}.
 */
class MempoolTest {

    @Test
    @DisplayName("Coinbase transactions are rejected by the mem-pool")
    void rejectCoinbase() {
        Wallet miner = new Wallet();
        Transaction coinbase = new Transaction(miner.getPublicKey(), 50.0, "0");

        Mempool mp = new Mempool();
        assertThrows(BlockchainException.class,
                     () -> mp.add(coinbase, Map.of()));
    }

    @Test
    @DisplayName("A correctly-signed ordinary TX is accepted and later purged")
    void addAndPurgeValidTx() {
        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();

        // --- fabricate a spendable UTXO ---------------------------------------
        String utxoId = "utxo123:0";
        TxOutput utxo = new TxOutput(10.0, alice.getPublicKey());

        // --- build + sign TX ---------------------------------------------------
        TxInput  in  = new TxInput(utxoId, null, alice.getPublicKey());
        TxOutput out = new TxOutput(10.0, bob.getPublicKey());

        Transaction payTx = new Transaction();
        payTx.getInputs().add(in);
        payTx.getOutputs().add(out);
        payTx.signInputs(alice.getPrivateKey());

        // --- mem-pool lifecycle ----------------------------------------------
        Mempool mp = new Mempool();
        mp.add(payTx, Map.of(utxoId, utxo));          // should succeed
        assertEquals(1, mp.take(10).size(), "TX present");

        mp.removeAll(mp.take(10));                    // simulate confirmation
        assertTrue(mp.take(10).isEmpty(), "TX purged");
    }

    @Test
    @DisplayName("Transactions are returned by descending tip")
    void orderedByTip() {
        Wallet w = new Wallet();
        String id1 = "utxo1:0";
        String id2 = "utxo2:0";

        TxOutput utxo1 = new TxOutput(10.0, w.getPublicKey());
        TxOutput utxo2 = new TxOutput(10.0, w.getPublicKey());

        Transaction highTip = new Transaction();
        highTip.getInputs().add(new TxInput(id1, null, w.getPublicKey()));
        highTip.getOutputs().add(new TxOutput(8.0, w.getPublicKey())); // maxFee 2
        highTip.setTip(1.5);
        highTip.signInputs(w.getPrivateKey());

        Transaction lowTip = new Transaction();
        lowTip.getInputs().add(new TxInput(id2, null, w.getPublicKey()));
        lowTip.getOutputs().add(new TxOutput(9.5, w.getPublicKey())); // maxFee 0.5
        lowTip.setTip(0.1);
        lowTip.signInputs(w.getPrivateKey());

        Mempool mp = new Mempool();
        mp.add(lowTip, Map.of(id1, utxo1, id2, utxo2));
        mp.add(highTip, Map.of(id1, utxo1, id2, utxo2));

        assertEquals(highTip, mp.take(2).get(0), "highest tip first");
    }

    @Test
    @DisplayName("Lowest fee transaction is evicted when full")
    void evictsLowestFee() {
        Wallet w = new Wallet();
        String id1 = "utxo1:0";
        String id2 = "utxo2:0";

        TxOutput utxo1 = new TxOutput(10.0, w.getPublicKey());
        TxOutput utxo2 = new TxOutput(10.0, w.getPublicKey());

        Mempool mp = new Mempool(1); // only keep one tx

        Transaction lowFee = new Transaction();
        lowFee.getInputs().add(new TxInput(id1, null, w.getPublicKey()));
        lowFee.getOutputs().add(new TxOutput(9.5, w.getPublicKey())); // fee 0.5
        lowFee.signInputs(w.getPrivateKey());

        Transaction highFee = new Transaction();
        highFee.getInputs().add(new TxInput(id2, null, w.getPublicKey()));
        highFee.getOutputs().add(new TxOutput(8.0, w.getPublicKey())); // fee 2
        highFee.signInputs(w.getPrivateKey());

        mp.add(lowFee, Map.of(id1, utxo1));
        mp.add(highFee, Map.of(id2, utxo2, id1, utxo1));

        assertEquals(1, mp.take(10).size(), "only one tx kept");
        assertEquals(highFee, mp.take(10).get(0), "low fee evicted");
    }
}
