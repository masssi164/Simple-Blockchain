package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

/**
 * Unit test for the NodeService.currentUtxoIncludingPending method.
 */
class NodeServicePendingUtxoTest {

    private NodeService       svc;
    private MempoolService    mempool;
    private Chain             chain;

    @BeforeEach
    void setUp() {
        // mock dependencies
        chain   = Mockito.mock(Chain.class);
        mempool = Mockito.mock(MempoolService.class);
        svc     = new NodeService(
            chain,
            mempool,
            Mockito.mock(MiningService.class),
            Mockito.mock(P2PBroadcastService.class),
            Mockito.mock(de.flashyotter.blockchain_node.storage.BlockStore.class),
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry()
        );
    }

    @Test
    void pendingSpendRemovesUTXO() {
        // 1) chain has one UTXO "utxo1" worth 10 coins to my address
        Wallet me = new Wallet();
        String myAddr = blockchain.core.crypto.AddressUtils.publicKeyToAddress(me.getPublicKey());
        TxOutput orig = new TxOutput(10.0, myAddr);
        Mockito.when(chain.getUtxoSnapshot())
               .thenReturn(Map.of("utxo1", orig));

        // 2) create a pending tx that spends "utxo1"
        Transaction pending = new Transaction();
        pending.getInputs().add(new TxInput("utxo1", new byte[0], me.getPublicKey()));
        pending.getOutputs().add(new TxOutput(7.5, "someOther"));   // pay 7.5 away
        pending.getOutputs().add(new TxOutput(2.5, me.getPublicKey())); // change
        pending.signInputs(me.getPrivateKey());

        Mockito.when(mempool.take(Integer.MAX_VALUE))
               .thenReturn(List.of(pending));

        // 3) get the effective UTXO set
        Map<String, TxOutput> effective = svc.currentUtxoIncludingPending();

        // Assertions:
        //   - original id "utxo1" must be removed
        //   - two new outputs appear: one at change address, one at "someOther"
        assertFalse(effective.containsKey("utxo1"));
        // find at least one output with value 2.5 at myAddr
        boolean hasChange = effective.values().stream()
            .anyMatch(o -> o.value() == 2.5 && o.recipientAddress().equals(myAddr));
        assertTrue(hasChange, "change output must be included");
    }
}
