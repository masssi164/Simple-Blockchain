package simple.blockchain.mempool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import blockchain.core.exceptions.BlockchainException;
import blockchain.core.mempool.Mempool;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

class MempoolEdgeCaseTest {

    @Test
    void utxoNotFoundIsRejected() {
        Mempool mp = new Mempool();
        Wallet alice = new Wallet();
        // TX referenziert eine nicht existierende UTXO
        Transaction tx = new Transaction();
        tx.getInputs().add(new TxInput("unknown:0", new byte[0], alice.getPublicKey()));
        tx.getOutputs().add(new TxOutput(5.0, alice.getPublicKey()));
        tx.signInputs(alice.getPrivateKey());

        BlockchainException ex = assertThrows(BlockchainException.class,
            () -> mp.add(tx, Map.of()));
        assertEquals("UTXO not found", ex.getMessage());
    }

    @Test
    void rejectDoubleSpendInPool() {
        Mempool mp = new Mempool();
        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();

        // eine echte, signierte UTXO
        String utxoId = "utxo123:0";
        TxOutput utxo = new TxOutput(10.0, alice.getPublicKey());
        Map<String, TxOutput> utxoMap = Map.of(utxoId, utxo);

        // erste TX
        Transaction tx1 = new Transaction();
        tx1.getInputs().add(new TxInput(utxoId, new byte[0], alice.getPublicKey()));
        tx1.getOutputs().add(new TxOutput(10.0, bob.getPublicKey()));
        tx1.signInputs(alice.getPrivateKey());

        // zweite TX, same input
        Transaction tx2 = new Transaction();
        tx2.getInputs().add(new TxInput(utxoId, new byte[0], alice.getPublicKey()));
        tx2.getOutputs().add(new TxOutput(10.0, bob.getPublicKey()));
        tx2.signInputs(alice.getPrivateKey());

        // erste rein
        mp.add(tx1, utxoMap);
        // zweite → "double-spend in mempool"
        BlockchainException ex = assertThrows(BlockchainException.class,
            () -> mp.add(tx2, utxoMap));
        assertEquals("double-spend in mempool", ex.getMessage());
    }
}
