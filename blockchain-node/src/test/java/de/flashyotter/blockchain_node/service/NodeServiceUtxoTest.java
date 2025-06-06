// blockchain-node/src/test/java/de/flashyotter/blockchain_node/service/NodeServiceUtxoTest.java
package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import blockchain.core.consensus.Chain;
import blockchain.core.model.TxOutput;
import de.flashyotter.blockchain_node.storage.InMemoryBlockStore;

/** Verifies that currentUtxo() reconstructs the full history (incl. genesis). */
class NodeServiceUtxoTest {

    private NodeService svc;

    @BeforeEach
    void init() {
        Chain chain = new Chain();                       // contains genesis only
        svc = new NodeService(
                chain,
                Mockito.mock(MempoolService.class),      // ↙ neue Reihenfolge
                Mockito.mock(MiningService.class),
                Mockito.mock(P2PBroadcastService.class),
                new InMemoryBlockStore()
        );
    }

    @Test
    void genesisOutputsPresent() {
        Map<String, TxOutput> utxo = svc.currentUtxo();
        assertEquals(1, utxo.size(), "UTXO must contain exactly the genesis output");
    }
}
