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
import java.util.concurrent.atomic.AtomicReference;
import java.nio.charset.StandardCharsets;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.PublicKey;
import java.io.IOException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
    private final AtomicReference<Map<String, TxOutput>>
            utxo = new AtomicReference<>(new ConcurrentHashMap<>());
    private final AtomicReference<Map<String, Integer>>
            coinbaseHeight = new AtomicReference<>(new ConcurrentHashMap<>());

    private volatile String bestTipHash;
    private int             currentBits = 0x1f0fffff;   // easy PoW für Demos

    private static final ObjectMapper LENGTH_MAPPER;

    static {
        SimpleModule pk = new SimpleModule();
        pk.addSerializer(PublicKey.class, new StdSerializer<>(PublicKey.class) {
            @Override
            public void serialize(PublicKey value, JsonGenerator gen,
                                   SerializerProvider serializers) throws IOException {
                gen.writeString(Base64.getEncoder().encodeToString(value.getEncoded()));
            }
        });

        LENGTH_MAPPER = new ObjectMapper()
                .findAndRegisterModules()
                .registerModule(pk)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /* ──────────────────────── Genesis Block ───────────────────────── */
    private static       Block   GENESIS;
    private static       String  GENESIS_HASH;
    private static       Wallet  GENESIS_WALLET;

    /* ───────────────────────── Konstruktor ────────────────────────── */
    public Chain() {
        ensureGenesis();
        indexBlock(GENESIS);
        switchActiveChain(GENESIS_HASH);   // füllt UTXO
    }

    private static synchronized void ensureGenesis() {
        if (GENESIS != null) return;
        if (GENESIS_WALLET == null) {
            GENESIS_WALLET = new Wallet();
        }
        GENESIS = createGenesis();
        GENESIS_HASH = GENESIS.getHashHex();
    }

    /* ───────────────────────── Read-only API ──────────────────────── */
    public Block                 getLatest()        { return activeChain.get(activeChain.size() - 1); }
    public List<Block>           getBlocks()        { return List.copyOf(activeChain); }
    public Map<String, TxOutput> getUtxoSnapshot()  { return Map.copyOf(utxo.get()); }
    public BigInteger            getTotalWork()     { return cumulativeWork.get(bestTipHash); }

    /** Height at which each coinbase output was created. */
    public Map<String, Integer> getCoinbaseHeightSnapshot() {
        return Map.copyOf(coinbaseHeight.get());
    }

    /** Replaces the UTXO and coinbase height maps with the provided snapshot. */
    public synchronized void loadUtxoSnapshot(Map<String, TxOutput> utxoMap,
                                               Map<String, Integer> heights) {
        utxo.set(new ConcurrentHashMap<>(utxoMap));
        coinbaseHeight.set(new ConcurrentHashMap<>(heights));
    }

    /* ───────────────────────── Block-Append ───────────────────────── */
    public synchronized void addBlock(Block b) {

        if (!"0".repeat(64).equals(b.getPreviousHashHex())
            && !allBlocks.containsKey(b.getPreviousHashHex()))
            throw new BlockchainException("prev-hash mismatch");

        validateTxs(b);

        if (!b.isProofValid())
            throw new BlockchainException("invalid PoW");

        int blockBytes;
        try {
            String json = LENGTH_MAPPER.writeValueAsString(b);
            blockBytes = json.getBytes(StandardCharsets.UTF_8).length;
        } catch (Exception e) {
            throw new BlockchainException("oversized block", e);
        }
        if (blockBytes > ConsensusParams.MAX_BLOCK_SIZE_BYTES
            || b.getTxList().size() > ConsensusParams.MAX_BLOCK_SIZE_BYTES)
            throw new BlockchainException("oversized block");

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
        Wallet miner = GENESIS_WALLET;
        Transaction cb = new Transaction(miner.getPublicKey(),
                                         ConsensusParams.blockReward(0),
                                         "GENESIS");
        return new Block(0, "0".repeat(64),
                         List.of(cb), 0x1f0fffff,
                         1_694_303_200_000L, 0);
    }

    /** Loads the private key for the genesis block from a PKCS#12 keystore.
     *  The keystore must contain exactly one key entry without a password. */
    public static synchronized void loadGenesisWallet(java.nio.file.Path keystore) {
        try {
            if (keystore == null) {
                GENESIS_WALLET = null;
                GENESIS = null;
                GENESIS_HASH = null;
                return;
            }
            java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
            try (var in = java.nio.file.Files.newInputStream(keystore)) {
                ks.load(in, new char[0]);
            }
            String alias = ks.aliases().nextElement();
            java.security.Key key = ks.getKey(alias, new char[0]);
            if (!(key instanceof java.security.PrivateKey priv))
                throw new BlockchainException("No private key in keystore");
            java.security.cert.Certificate cert = ks.getCertificate(alias);
            java.security.PublicKey pub = (cert != null)
                    ? cert.getPublicKey()
                    : blockchain.core.crypto.CryptoUtils.derivePublicKey(priv);
            GENESIS_WALLET = new Wallet(priv, pub);
            GENESIS = null;
            GENESIS_HASH = null;
        } catch (Exception e) {
            throw new BlockchainException("load genesis wallet", e);
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
        Set<String> newCoinbase = new HashSet<>();
        for (Transaction tx : b.getTxList()) {
            int idx = 0;
            for (TxOutput out : tx.getOutputs()) {
                String id = out.id(tx.calcHashHex(), idx++);
                newOutputs.put(id, out);
                if (tx.isCoinbase()) newCoinbase.add(id);
            }
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

                TxOutput out = utxo.get().get(ref);
                Integer created = coinbaseHeight.get().get(ref);
                if (out == null) {
                    out = newOutputs.get(ref);
                    if (newCoinbase.contains(ref)) created = b.getHeight();
                }
                if (out == null)
                    throw new BlockchainException("UTXO not found");

                if (created != null && b.getHeight() - created < ConsensusParams.COINBASE_MATURITY)
                    throw new BlockchainException("coinbase immature");

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
        Map<String, TxOutput>  newUtxo    = new ConcurrentHashMap<>();
        Map<String, Integer>   newHeights = new ConcurrentHashMap<>();
        branchRev.forEach(b -> updateUtxo(newUtxo, newHeights, b));
        utxo.set(Collections.unmodifiableMap(newUtxo));
        coinbaseHeight.set(Collections.unmodifiableMap(newHeights));

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
    private void updateUtxo(Map<String, TxOutput> utxoMap,
                            Map<String, Integer> coinbaseMap,
                            Block b) {
        for (Transaction tx : b.getTxList()) {

            /* zuerst: alle referenzierten Ausgänge “verbrauchen” */
            if (!tx.isCoinbase()) {
                tx.getInputs().forEach(in -> {
                    utxoMap.remove(in.getReferencedOutputId());
                    coinbaseMap.remove(in.getReferencedOutputId());
                });
            }

            /* danach: neue Outputs verfügbar machen */
            int idx = 0;
            for (TxOutput out : tx.getOutputs()) {
                String id = out.id(tx.calcHashHex(), idx++);
                utxoMap.put(id, out);
                if (tx.isCoinbase()) coinbaseMap.put(id, b.getHeight());
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

    /**
     * Removes old blocks from the internal DAG maps while keeping the active
     * chain untouched. Returns the removed blocks so callers can persist them
     * via a {@code BlockStore} if desired.
     *
     * @param keep how many recent blocks (by height) to retain for forks
     * @return list of pruned blocks
     */
    public synchronized List<Block> pruneOldBlocks(int keep) {
        int cutoff = Math.max(0, getLatest().getHeight() - keep);

        // Build set of hashes on the active chain so those are never removed
        Set<String> active = new HashSet<>();
        for (Block b : activeChain) active.add(b.getHashHex());

        List<Block> removed = new ArrayList<>();
        for (var entry : new ArrayList<>(allBlocks.entrySet())) {
            Block blk = entry.getValue();
            if (blk.getHeight() <= cutoff && !active.contains(entry.getKey())) {
                removed.add(blk);
                allBlocks.remove(entry.getKey());
                cumulativeWork.remove(entry.getKey());
                parent.remove(entry.getKey());
            }
        }
        return removed;
    }
}
