package de.flashyotter.blockchain_node.wallet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.crypto.CryptoUtils;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.MnemonicUtils;
import de.flashyotter.blockchain_node.wallet.KeyStoreProvider;

@Service
@Slf4j
public class WalletService {

    private static final String META_FILE = "hd-wallet.json";
    private static final int    HARDENED  = 0x80000000;

    private final Path            metaPath;
    private final ObjectMapper    mapper = new ObjectMapper();

    @Getter private final Wallet localWallet;
    @Getter private       String mnemonic;
    private int nextIndex;

    public WalletService(KeyStoreProvider store,
                         @Value("${wallet.store-path}") String storePath) {
        Path ks = Path.of(storePath);
        if (!ks.isAbsolute()) {
            ks = Path.of(System.getProperty("user.home"), storePath);
        }
        this.metaPath = ks.getParent().resolve(META_FILE);
        loadOrCreateMeta();
        this.localWallet = deriveWallet(0);
        log.info("Wallet address → {}", AddressUtils.publicKeyToAddress(localWallet.getPublicKey()));
    }

    /* ── persist / restore ─────────────────────────────────────────── */

    private void loadOrCreateMeta() {
        if (Files.exists(metaPath)) {
            try {
                var info = mapper.readValue(metaPath.toFile(), HdInfo.class);
                this.mnemonic = info.mnemonic;
                this.nextIndex = info.nextIndex;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read wallet metadata", e);
            }
            return;
        }

        byte[] entropy = new byte[16];
        new SecureRandom().nextBytes(entropy);
        this.mnemonic = MnemonicUtils.generateMnemonic(entropy);
        this.nextIndex = 1; // index 0 reserved for localWallet

        try {
            Files.createDirectories(metaPath.getParent());
            mapper.writeValue(metaPath.toFile(), new HdInfo(mnemonic, nextIndex));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store wallet metadata", e);
        }
    }

    private void persistMeta() {
        try {
            mapper.writeValue(metaPath.toFile(), new HdInfo(mnemonic, nextIndex));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store wallet metadata", e);
        }
    }

    private Wallet deriveWallet(int index) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
        Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
        int[] path = {
                44 | HARDENED,
                0  | HARDENED,
                0  | HARDENED,
                0,
                index
        };
        Bip32ECKeyPair key = Bip32ECKeyPair.deriveKeyPair(master, path);
        PrivateKey priv = CryptoUtils.privateKeyFromBigInt(key.getPrivateKey());
        PublicKey pub = CryptoUtils.derivePublicKey(priv);
        return new Wallet(priv, pub);
    }

    public synchronized Wallet newAddress() {
        Wallet w = deriveWallet(nextIndex);
        nextIndex++;
        persistMeta();
        return w;
    }

    private record HdInfo(String mnemonic, int nextIndex) {}

    /* ── public API ────────────────────────────────────────────────── */

    public Transaction createTx(String recipientAddr,
                                double amount,
                                Map<String, TxOutput> utxo) {
        return localWallet.sendFunds(recipientAddr, amount, utxo);
    }

    /** Current confirmed balance of this wallet. */
    public double balance(Map<String, TxOutput> utxo) {
        String myAddr = AddressUtils.publicKeyToAddress(localWallet.getPublicKey());
        return utxo.values().stream()
                .filter(out -> out.recipientAddress().equals(myAddr))
                .mapToDouble(TxOutput::value)
                .sum();
    }

}
