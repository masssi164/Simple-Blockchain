package simple.blockchain.mempool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Transaction coinbase = new Transaction(miner.getPublicKey(), 50.0);

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
}
