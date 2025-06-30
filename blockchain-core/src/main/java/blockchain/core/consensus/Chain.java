// blockchain-core/src/main/java/blockchain/core/consensus/Chain.java
package blockchain.core.consensus;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.crypto.HashingUtils;
import blockchain.core.exceptions.BlockchainException;
import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import blockchain.core.consensus.ConsensusParams;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe blockchain that maintains *all* seen blocks (DAG),
 * automatically switches to the branch with the greatest cumulative
 * Proof-of-Work and rebuilds the UTXO set on each re-organisation.
 */
@Slf4j
public class Chain {

    /* ─────────────────────── DAG Strukturen ──────────────────────── */
    private final Map<String, Block>            allBlocks      = new ConcurrentHashMap<>();
    private final Map<String, BigInteger>       cumulativeWork = new ConcurrentHashMap<>();
    private final Map<String, String>           parent         = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Block>   activeChain    = new CopyOnWriteArrayList<>();
    private final Map<String, TxOutput>         utxo           = new ConcurrentHashMap<>();

    private volatile String bestTipHash;
    private int             currentBits = 0x1f0fffff;   // easy PoW für Demos

    /* ──────────────────────── Genesis Block ───────────────────────── */
    private static final Block  GENESIS;
    private static final String GENESIS_HASH;

    static {
        GENESIS = createGenesis();
        GENESIS_HASH = GENESIS.getHashHex();
    }

    /* ───────────────────────── Konstruktor ────────────────────────── */
    public Chain() {
        indexBlock(GENESIS);
        switchActiveChain(GENESIS_HASH);   // füllt UTXO
    }

    /* ───────────────────────── Read-only API ──────────────────────── */
    public Block                 getLatest()        { return activeChain.get(activeChain.size() - 1); }
    public List<Block>           getBlocks()        { return List.copyOf(activeChain); }
    public Map<String, TxOutput> getUtxoSnapshot()  { return Map.copyOf(utxo); }
    public BigInteger            getTotalWork()     { return cumulativeWork.get(bestTipHash); }

    /* ───────────────────────── Block-Append ───────────────────────── */
    public void addBlock(Block b) {

        if (!"0".repeat(64).equals(b.getPreviousHashHex())
            && !allBlocks.containsKey(b.getPreviousHashHex()))
            throw new BlockchainException("prev-hash mismatch");

        validateTxs(b);

        if (!b.isProofValid())
            throw new BlockchainException("invalid PoW");

        indexBlock(b);

        BigInteger newWork  = cumulativeWork.get(b.getHashHex());
        BigInteger bestWork = cumulativeWork.get(bestTipHash);
        if (newWork.compareTo(bestWork) > 0) {
            log.info("⛓️  re-org → new tip {} (h={})  work={}",
                     b.getHashHex().substring(0, 8), b.getHeight(), newWork);
            switchActiveChain(b.getHashHex());
        }
    }

    /* ─────────────────────── Genesis & Helpers ────────────────────── */
    private static Block createGenesis() {
        Wallet miner = deterministicWallet();
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(0),
                                         "GENESIS");
        return new Block(0, "0".repeat(64),
                         List.of(cb), 0x1f0fffff,
                         1_694_303_200_000L, 0);
    }

    private static Wallet deterministicWallet() {
        try {
            java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("simple-chain-genesis".getBytes());
            kpg.initialize(new ECGenParameterSpec("secp256k1"), sr);
            KeyPair kp = kpg.generateKeyPair();
            return new Wallet(kp.getPrivate(), kp.getPublic());
        } catch (GeneralSecurityException e) {
            throw new BlockchainException("genesis key", e);
        }
    }

    private void validateTxs(Block b) {
        if (b.getTxList().isEmpty())            throw new BlockchainException("empty block");

        Transaction coinbase = b.getTxList().get(0);
        if (!coinbase.isCoinbase()) throw new BlockchainException("first tx must be coinbase");

        double reward = coinbase.getOutputs().stream().mapToDouble(TxOutput::value).sum();
        if (reward > ConsensusParams.blockReward(b.getHeight()))
            throw new BlockchainException("excessive coinbase");

        /* 1) Alle neuen Outputs der Block-Transaktionen sammeln -------- */
        Map<String, TxOutput> newOutputs = new ConcurrentHashMap<>();
        for (Transaction tx : b.getTxList()) {
            int idx = 0;
            for (TxOutput out : tx.getOutputs())
                newOutputs.put(out.id(tx.calcHashHex(), idx++), out);
        }

        /* 2) Inputs validieren, inkl. Ausgänge aus diesem Block --------- */
        Set<String> spent = new HashSet<>();

        for (Transaction tx : b.getTxList().subList(1, b.getTxList().size())) {
            if (!tx.verifySignatures())
                throw new BlockchainException("bad signature");

            for (var in : tx.getInputs()) {
                String ref = in.getReferencedOutputId();
                if (spent.contains(ref))
                    throw new BlockchainException("double-spend in block");

                TxOutput out = utxo.get(ref);
                if (out == null) out = newOutputs.get(ref);
                if (out == null)
                    throw new BlockchainException("UTXO not found");

                String sender = AddressUtils.publicKeyToAddress(in.getSender());
                if (!sender.equals(out.recipientAddress()))
                    throw new BlockchainException("pub-key mismatch");

                spent.add(ref);
            }
        }
    }

    /* ────────── Index in globale DAG ────────── */
    private void indexBlock(Block b) {
        String h = b.getHashHex();
        allBlocks.put(h, b);
        parent.put(h, b.getPreviousHashHex());

        BigInteger parentWork = "0".repeat(64).equals(b.getPreviousHashHex())
                ? BigInteger.ZERO
                : cumulativeWork.get(b.getPreviousHashHex());
        cumulativeWork.put(h, parentWork.add(workForBits(b.getCompactDifficultyBits())));
    }

    /* ────────── Active-Branch Switch / Re-org ────────── */
    private synchronized void switchActiveChain(String newTipHash) {

        /* 1) Pfad von neuem Tip bis Genesis sammeln */
        List<Block> branchRev = new ArrayList<>();
        String ptr = newTipHash;
        while (ptr != null && allBlocks.containsKey(ptr)) {
            branchRev.add(allBlocks.get(ptr));
            ptr = parent.get(ptr);
        }
        Collections.reverse(branchRev); // genesis … newTip

        /* 2) UTXO neu aufbauen (Inputs abziehen, Outputs hinzufügen) */
        utxo.clear();
        branchRev.forEach(this::updateUtxo);

        /* 3) Aktive Kette austauschen */
        activeChain.clear();
        activeChain.addAll(branchRev);

        bestTipHash = newTipHash;
    }

    /**
     * Fügt sämtliche Outputs des Blocks hinzu **und** entfernt alle durch
     * Inputs verbrauchten UTXOs – dadurch bleiben nur ungenutzte Ausgänge
     * übrig (klassisches UTXO-Modell).
     */
    private void updateUtxo(Block b) {
        for (Transaction tx : b.getTxList()) {

            /* zuerst: alle referenzierten Ausgänge “verbrauchen” */
            if (!tx.isCoinbase()) {
                tx.getInputs()
                  .forEach(in -> utxo.remove(in.getReferencedOutputId()));
            }

            /* danach: neue Outputs verfügbar machen */
            int idx = 0;
            for (TxOutput out : tx.getOutputs()) {
                utxo.put(out.id(tx.calcHashHex(), idx++), out);
            }
        }
    }

    private static BigInteger workForBits(int bits) {
        return BigInteger.ONE.shiftLeft(256)
                             .divide(HashingUtils.compactToTarget(bits));
    }

    /** Calculates new bits every window; else returns current value. */
    public synchronized int nextCompactBits() {
        if (getLatest().getHeight() % ConsensusParams.RETARGET_SPAN != 0
            || getLatest().getHeight() == 0)
            return currentBits;

        Block tail = activeChain.get(activeChain.size() - ConsensusParams.RETARGET_SPAN);
        long diff   = getLatest().getTimeMillis() - tail.getTimeMillis();

        long   target = ConsensusParams.RETARGET_TIMESPAN_MS;
        double factor = Math.max(0.25, Math.min(4.0, (double) diff / target));

        BigInteger oldTarget = HashingUtils.compactToTarget(currentBits);
        BigInteger newTarget = oldTarget.multiply(BigInteger.valueOf((long) (factor * 1_000_000)))
                                        .divide(BigInteger.valueOf(1_000_000));
        currentBits = HashingUtils.targetToCompact(newTarget);
        return currentBits;
    }
}
