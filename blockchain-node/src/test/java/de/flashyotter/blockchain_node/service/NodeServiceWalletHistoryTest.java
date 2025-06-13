package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import blockchain.core.consensus.Chain;
import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxInput;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;

class NodeServiceWalletHistoryTest {

    private Chain chain;
    private NodeService svc;

    @BeforeEach
    void setUp() {
        chain = Mockito.mock(Chain.class);
        svc = new NodeService(chain,
                Mockito.mock(MempoolService.class),
                Mockito.mock(MiningService.class),
                Mockito.mock(P2PBroadcastService.class),
                Mockito.mock(de.flashyotter.blockchain_node.storage.BlockStore.class));
    }

    @Test
    void findsRecentTransactionsForAddress() {
        // collects the latest transactions touching an address
        Wallet w = new Wallet();
        String addr = AddressUtils.publicKeyToAddress(w.getPublicKey());

        Transaction tOut = new Transaction();
        tOut.getOutputs().add(new TxOutput(1.0, addr));

        Transaction tIn = new Transaction();
        tIn.getInputs().add(new TxInput("foo", new byte[0], w.getPublicKey()));

        List<Block> blocks = List.of(
            new Block(0, "a", List.of(tOut), 0),
            new Block(1, "b", List.of(tIn), 0)
        );
        when(chain.getBlocks()).thenReturn(blocks);

        List<Transaction> hist = svc.walletHistory(addr, 10);
        assertEquals(2, hist.size());
        assertEquals(tIn, hist.get(0));
    }
}
