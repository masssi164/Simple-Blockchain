package simple.blockchain.Architecture;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import simple.blockchain.Utils.HashUtil;
import simple.blockchain.exceptions.BlockchainException;

@Getter
@Slf4j
public class Chain {

    private static final long TARGET_MS = 60_000;
    private static final int  INTERVAL  = 10;

    private int bits = 0x1f0fffff;

    private final List<Block>           blocks = new ArrayList<>();
    private final Map<String, TxOutput> utxo   = new HashMap<>();

    public Chain() { blocks.add(genesis()); }

    /* ---------- Blöcke ---------- */

    public void addBlock(Block b) {
        for (Transaction tx : b.getTransactions())
            if (!validateTx(tx)) throw new BlockchainException("TX invalid");

        b.mine();
        blocks.add(b);
        updateUtxo(b);
        retarget();
    }

    public Block getLatest() { return blocks.get(blocks.size() - 1); }

    /* ---------- Genesis ---------- */

    private Block genesis() {
        Wallet w = new Wallet();
        Transaction cb = new Transaction(w.getPublicKey(), 50);
        Block g = new Block(0, "0".repeat(64), List.of(cb), bits);
        updateUtxo(g);
        return g;
    }

    /* ---------- Difficulty ---------- */

    private void retarget() {
        int h = blocks.size() - 1;
        if (h == 0 || h % INTERVAL != 0) return;

        long actual   = blocks.get(h).getTimeStamp() - blocks.get(h - INTERVAL).getTimeStamp();
        long expected = TARGET_MS * INTERVAL;

        BigInteger target = HashUtil.compactToBigInt(bits);
        if (actual < expected / 2)      target = target.shiftRight(1);
        else if (actual > expected * 2) target = target.shiftLeft(1);

        bits = HashUtil.bigIntToCompact(target);
        log.info("Difficulty retarget → bits=0x{}", Integer.toHexString(bits));
    }

    /* ---------- TX / UTXO ---------- */

    private boolean validateTx(Transaction tx) {
        if (!tx.verifySignatures()) return false;
        if (tx.isCoinbase()) return true;

        double inSum = 0, outSum = 0;
        for (TxInput in : tx.getInputs()) {
            TxOutput ref = utxo.get(in.getReferencedOutputId());
            if (ref == null || !ref.getRecipient().equals(in.getSender())) return false;
            inSum += ref.getValue();
        }
        for (TxOutput o : tx.getOutputs()) outSum += o.getValue();
        return inSum >= outSum;
    }

    private void updateUtxo(Block b) {
        for (Transaction tx : b.getTransactions()) {
            tx.getInputs().forEach(in -> utxo.remove(in.getReferencedOutputId()));
            int idx = 0;
            for (TxOutput out : tx.getOutputs())
                utxo.put(out.id(tx.calcHash(), idx++), out);
        }
    }
}
