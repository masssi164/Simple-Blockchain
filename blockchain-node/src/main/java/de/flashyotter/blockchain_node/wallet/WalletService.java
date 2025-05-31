package de.flashyotter.blockchain_node.wallet;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import blockchain.core.crypto.CryptoUtils;
import blockchain.core.model.Transaction;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WalletService {

    private static final String KEY_ALIAS = "simplechain";
    private final KeyStoreProvider store;
    @Getter private final Wallet localWallet;

    public WalletService(KeyStoreProvider store) {
        this.store = store;
        this.localWallet = loadOrCreate();
        String pubB64 = CryptoUtils.keyToBase64(localWallet.getPublicKey());
        log.info("Wallet address → {}", pubB64);
    }

    private Wallet loadOrCreate() {
        Optional<PrivateKey> maybePriv;
        try {
            maybePriv = store.load(KEY_ALIAS);
        } catch (GeneralSecurityException e) {
            log.warn("Private-key load failed: {}", e.getMessage());
            maybePriv = Optional.empty();
        }

        if (maybePriv.isPresent()) {
            PrivateKey priv = maybePriv.get();
            PublicKey pub;

            // default profile: pull the public straight out of our PKCS12 cert
            if (store instanceof PkcsKeyStoreProvider) {
                try {
                    pub = ((PkcsKeyStoreProvider) store).loadPublicKey(KEY_ALIAS);
                } catch (Exception e) {
                    log.warn("Public-key load from keystore failed: {}; deriving instead", e.getMessage());
                    pub = CryptoUtils.derivePublicKey(priv);
                }
            } else {
                // e.g. InMemoryKeyStore in tests
                pub = CryptoUtils.derivePublicKey(priv);
            }

            log.info("🔑 Loaded existing wallet");
            return new Wallet(priv, pub);
        }

        // no wallet on disk → new one
        Wallet fresh = new Wallet();
        persist(fresh);
        log.info("🆕 Generated new wallet");
        return fresh;
    }

    private void persist(Wallet w) {
        try {
            store.save(KEY_ALIAS, w.getPrivateKey());
        } catch (GeneralSecurityException e) {
            log.error("Private-key save failed: {}", e.getMessage());
            throw new RuntimeException("Failed to save wallet", e);
        }
    }

    public Transaction createTx(String recipientB64, double amount, Map<String, TxOutput> utxo) {
        var recipient = CryptoUtils.publicKeyFromBase64(recipientB64);
        return localWallet.sendFunds(recipient, amount, utxo);
    }

    public double balance(Map<String, TxOutput> utxo) {
        return utxo.values().stream()
                .filter(out -> out.recipient().equals(localWallet.getPublicKey()))
                .mapToDouble(TxOutput::value)
                .sum();
    }
}
