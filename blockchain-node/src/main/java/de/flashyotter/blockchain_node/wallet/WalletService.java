package de.flashyotter.blockchain_node.wallet;

import blockchain.core.crypto.CryptoUtils;
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
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Loads or creates the local key-pair and exposes helper
 * methods (balance, createTx).
 */
@Service @Slf4j
public class WalletService {

    private static final Path FILE = Path.of("data", "wallet.json");
    private static final ObjectMapper M = new ObjectMapper();

    @Getter
    private final Wallet localWallet = loadOrCreate();

    /* ────────────────────────────────────────────────────────────── */

    public double balance(Map<String, TxOutput> utxo) {
        return utxo.values().stream()
                   .filter(o -> o.recipient().equals(localWallet.getPublicKey()))
                   .mapToDouble(TxOutput::value)
                   .sum();
    }

    /**
     * Crafts & signs a TX that pays {@code amount} to {@code toBase64}.
     * Caller must still submit it to the node / network.
     */
    public Transaction createTx(String toBase64,
                                double amount,
                                Map<String, TxOutput> utxoSnapshot) {

        try {
            byte[] pubBytes = Base64.getDecoder().decode(toBase64);
            PublicKey toKey = KeyFactory.getInstance("EC")
                                        .generatePublic(new X509EncodedKeySpec(pubBytes));
            return localWallet.sendFunds(toKey, amount, utxoSnapshot);

        } catch (Exception e) {
            throw new IllegalArgumentException("recipient key invalid", e);
        }
    }

    /* ────────────────────────────────────────────────────────────── */

    private Wallet loadOrCreate() {
        try {
            if (Files.exists(FILE)) {
                Map<?, ?> json = M.readValue(Files.readAllBytes(FILE), Map.class);
                byte[] privBytes = Base64.getDecoder().decode((String) json.get("priv"));
                byte[] pubBytes  = Base64.getDecoder().decode((String) json.get("pub"));

                var kf   = KeyFactory.getInstance("EC");
                var priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
                var pub  = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
                return new Wallet(priv, pub);
            }

            Wallet fresh = new Wallet();
            persist(fresh);
            return fresh;

        } catch (Exception e) {
            throw new RuntimeException("wallet bootstrap failed", e);
        }
    }

    private void persist(Wallet w) throws IOException {
        Files.createDirectories(FILE.getParent());
        M.writerWithDefaultPrettyPrinter().writeValue(FILE.toFile(),
                Map.of("priv", CryptoUtils.keyToBase64(w.getPrivateKey()),
                       "pub",  CryptoUtils.keyToBase64(w.getPublicKey())));
    }
}
