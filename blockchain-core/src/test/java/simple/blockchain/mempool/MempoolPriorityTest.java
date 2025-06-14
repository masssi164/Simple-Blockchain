package simple.blockchain.mempool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import blockchain.core.mempool.Mempool;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/** Tests for fee prioritisation in the mem-pool. */
class MempoolPriorityTest {

    @Test
    @DisplayName("Higher fee transactions replace lower fee ones when full")
    void replacesLowFeeTx() {
        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();

        Map<String, TxOutput> utxo = new HashMap<>();
        utxo.put("a:0", new TxOutput(10.0, alice.getPublicKey()));
        utxo.put("b:0", new TxOutput(10.0, alice.getPublicKey()));

        Transaction lowFee = new Transaction();
        lowFee.getInputs().add(new TxInput("a:0", new byte[0], alice.getPublicKey()));
        lowFee.getOutputs().add(new TxOutput(10.0, bob.getPublicKey()));
        lowFee.signInputs(alice.getPrivateKey());

        Transaction highFee = new Transaction();
        highFee.getInputs().add(new TxInput("b:0", new byte[0], alice.getPublicKey()));
        highFee.getOutputs().add(new TxOutput(9.0, bob.getPublicKey()));
        highFee.signInputs(alice.getPrivateKey());

        Mempool mp = new Mempool(1);
        mp.add(lowFee, utxo);
        mp.add(highFee, utxo);

        assertEquals(highFee.calcHashHex(), mp.take(10).get(0).calcHashHex());
    }
}
