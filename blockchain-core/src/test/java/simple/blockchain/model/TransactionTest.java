package simple.blockchain.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/**
 * Unit tests for (de)signing behaviour of {@link Transaction}.
 */
class TransactionTest {

    @Test
    @DisplayName("ECDSA signatures produced by Wallet verify successfully")
    void signAndVerify() {
        Wallet w = new Wallet();

        // dummy referenced UTXO id – content irrelevant for sig / verify -------
        String refId = "abcd1234:0";

        TxInput in  = new TxInput(refId, null, w.getPublicKey());
        TxOutput out = new TxOutput(5.0, w.getPublicKey());

        Transaction tx = new Transaction();
        tx.getInputs().add(in);
        tx.getOutputs().add(out);

        tx.signInputs(w.getPrivateKey());
        assertTrue(tx.verifySignatures(), "all inputs verify");
    }
}
