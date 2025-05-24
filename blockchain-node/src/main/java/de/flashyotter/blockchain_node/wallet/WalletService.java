package de.flashyotter.blockchain_node.wallet;

import blockchain.core.crypto.CryptoUtils;
import blockchain.core.crypto.KeychainBox;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Non-custodial wallet that keeps the **private key in the macOS
 * login keychain** (alias {@code simple-blockchain-wallet}).  
 *
 * If the Keychain provider is not available (e.g. Linux, Windows),
 * we silently fall back to the old behaviour: only the <b>public</b>
 * key is written to {@code data/wallet.json}; the private key lives
 * in memory for the lifetime of the JVM and is lost afterwards.
 */
@Service
@Slf4j
public class WalletService {

    private static final String KEYCHAIN_ALIAS = "simple-blockchain-wallet";
    private static final Path   PUB_FILE       = Path.of("data", "wallet.json");
    private static final ObjectMapper MAPPER   = new ObjectMapper();

    @Getter
    private final Wallet localWallet = loadOrCreate();

    /* ────────────────────────────────────────────────────────────── */

    public double balance(Map<String, TxOutput> utxo) {
        return utxo.values().stream()
                   .filter(o -> o.recipient().equals(localWallet.getPublicKey()))
                   .mapToDouble(TxOutput::value)
                   .sum();
    }

    public Transaction createTx(String recipientBase64,
                                double amount,
                                Map<String, TxOutput> utxoSnapshot) {

        try {
            byte[] pubBytes = Base64.getDecoder().decode(recipientBase64);
            PublicKey toKey = KeyFactory.getInstance("EC")
                                        .generatePublic(new X509EncodedKeySpec(pubBytes));
            return localWallet.sendFunds(toKey, amount, utxoSnapshot);

        } catch (Exception e) {
            throw new IllegalArgumentException("recipient key invalid", e);
        }
    }

    /* ────────────────────────────────────────────────────────────── */
    /*                       bootstrap & storage                     */
    /* ────────────────────────────────────────────────────────────── */

    private Wallet loadOrCreate() {

        /* ① try macOS keychain ------------------------------------------------ */
        try {
            Optional<PrivateKey> keyOpt = KeychainBox.load(KEYCHAIN_ALIAS);
            if (keyOpt.isPresent()) {
                PrivateKey priv = keyOpt.get();
                PublicKey  pub  = readPublicKey();
                log.info("🔑  Wallet loaded from macOS keychain (alias={})", KEYCHAIN_ALIAS);
                return new Wallet(priv, pub);
            }
        } catch (Exception e) {
            log.warn("Keychain provider not available – fallback to file based wallet ({})", e.getMessage());
        }

        /* ② nothing found → create a fresh key-pair --------------------------- */
        Wallet fresh = new Wallet();
        persist(fresh);
        log.info("🆕  New wallet generated");
        return fresh;
    }

    private void persist(Wallet w) {

        /* a) Always persist PUBLIC key → wallet.json (human-readable, non-secret) */
        try {
            Files.createDirectories(PUB_FILE.getParent());
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(PUB_FILE.toFile(),
                    Map.of("pub", CryptoUtils.keyToBase64(w.getPublicKey())));
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write wallet.json", ex);
        }

        /* b) Try to put PRIVATE key into the macOS keychain (no-op elsewhere)  */
        try {
            KeychainBox.store(KEYCHAIN_ALIAS, w.getPrivateKey());
            log.info("🔐  Private key stored in keychain (alias={})", KEYCHAIN_ALIAS);
        } catch (Exception ignored) {
            // Not macOS or no Security CLI – fine; the key just lives in memory
        }
    }

    /* helper ----------------------------------------------------------------- */

    private PublicKey readPublicKey() {
        try {
            if (!Files.exists(PUB_FILE))
                throw new IllegalStateException("wallet.json missing – cannot recover public key");

            Map<?, ?> json = MAPPER.readValue(Files.readAllBytes(PUB_FILE), Map.class);
            byte[] pubBytes = Base64.getDecoder().decode((String) json.get("pub"));
            return KeyFactory.getInstance("EC")
                             .generatePublic(new X509EncodedKeySpec(pubBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read public key", e);
        }
    }
}
