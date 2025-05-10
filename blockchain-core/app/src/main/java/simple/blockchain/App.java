package simple.blockchain;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import simple.blockchain.Architecture.*;

@Slf4j
public class App {

    public static void main(String[] args) {

        Chain chain = new Chain();

        Wallet miner = new Wallet();
        Wallet alice = new Wallet();
        Wallet bob   = new Wallet();

        /* Block 1 – Coinbase */
        Transaction cb1 = new Transaction(miner.getPublicKey(), 50);
        chain.addBlock(new Block(1, chain.getLatest().getHash(), List.of(cb1), chain.getBits()));

        /* Block 2 – Miner → Alice */
        Transaction t1 = miner.sendFunds(alice.getPublicKey(), 25, chain.getUtxo());
        chain.addBlock(new Block(2, chain.getLatest().getHash(), List.of(t1), chain.getBits()));

        /* Block 3 – Alice → Bob */
        Transaction t2 = alice.sendFunds(bob.getPublicKey(), 10, chain.getUtxo());
        chain.addBlock(new Block(3, chain.getLatest().getHash(), List.of(t2), chain.getBits()));

        chain.getBlocks().forEach(b -> log.info("{}", b));
    }
}
