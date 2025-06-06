package de.flashyotter.blockchain_node.wallet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/**
 * Exercises the public Wallet API used by the REST layer, including
 * automatic change output generation and signature validity.
 */
class WalletServiceIntegrationTest {

    @Test
    @DisplayName("sendFunds() selects inputs, adds change and produces valid signatures")
    void walletCreatesBalancedSignedTx() {
        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();

        /* fabricate two spendable UTXOs for Alice ---------------------- */
        String utxo1 = "a1:0";
        String utxo2 = "a2:0";

        Map<String, TxOutput> utxoMap = Map.of(
                utxo1, new TxOutput(6.0, alice.getPublicKey()),
                utxo2, new TxOutput(5.0, alice.getPublicKey()));

        /* build transaction: pay 8 coins to Bob ------------------------ */
        Transaction tx = alice.sendFunds(
                AddressUtils.publicKeyToAddress(bob.getPublicKey()),
                8.0,
                utxoMap);

        /* assertions ---------------------------------------------------- */
        assertEquals(2, tx.getInputs().size(),      "both inputs consumed");
        assertEquals(2, tx.getOutputs().size(),     "payment + change");

        // output[0] → Bob (8.0), output[1] → change (3.0 to Alice)
        assertEquals(8.0, tx.getOutputs().get(0).value(), 1e-9);
        assertEquals(3.0, tx.getOutputs().get(1).value(), 1e-9);

        assertTrue(tx.verifySignatures(), "ECDSA signatures verify");
    }
}
