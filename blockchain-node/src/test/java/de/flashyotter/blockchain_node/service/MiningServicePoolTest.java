package de.flashyotter.blockchain_node.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import blockchain.core.consensus.Chain;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.Wallet;
import de.flashyotter.blockchain_node.config.NodeProperties;
import de.flashyotter.blockchain_node.wallet.WalletService;

class MiningServicePoolTest {

    private Chain chain;
    private MempoolService mempool;
    private WalletService wallet;
    private NodeProperties props;
    private MiningService svc;

    @BeforeEach
    void setUp() throws Exception {
        chain = mock(Chain.class);
        mempool = mock(MempoolService.class);
        wallet = mock(WalletService.class);
        props = new NodeProperties();
        props.setMiningThreads(2);

        Wallet w = new Wallet();
        when(wallet.getLocalWallet()).thenReturn(w);

        Transaction coinbase = new Transaction(w.getPublicKey(), 0.0, "0");
        Block latest = new Block(0, "0", List.of(coinbase), 0x207fffff, 1L, 0);
        when(chain.getLatest()).thenReturn(latest);
        when(chain.nextCompactBits()).thenReturn(0x207fffff);

        when(mempool.take(anyInt())).thenReturn(List.of());
        when(mempool.getBaseFee()).thenReturn(0.0);
        when(mempool.tipFor(org.mockito.ArgumentMatchers.any())).thenReturn(0.0);

        svc = new MiningService(chain, mempool, wallet, props);
        Method init = MiningService.class.getDeclaredMethod("initPool");
        init.setAccessible(true);
        init.invoke(svc);
    }

    @Test
    void poolIsReusedAndShutdown() throws Exception {
        Field poolField = MiningService.class.getDeclaredField("pool");
        poolField.setAccessible(true);

        ForkJoinPool first = (ForkJoinPool) poolField.get(svc);
        assertNotNull(first);

        svc.mine();
        svc.mine();

        ForkJoinPool second = (ForkJoinPool) poolField.get(svc);
        assertSame(first, second, "pool should be reused");

        Method shutdown = MiningService.class.getDeclaredMethod("shutdownPool");
        shutdown.setAccessible(true);
        shutdown.invoke(svc);

        assertTrue(first.isShutdown(), "pool should shut down on destroy");
    }
}
