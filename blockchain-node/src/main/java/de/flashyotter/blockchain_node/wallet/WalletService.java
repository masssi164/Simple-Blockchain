package de.flashyotter.blockchain_node.wallet;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import blockchain.core.crypto.AddressUtils;
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
        log.info("Wallet address → {}", AddressUtils.publicKeyToAddress(localWallet.getPublicKey()));
    }

    /* ── persist / restore ─────────────────────────────────────────── */

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
            if (store instanceof PkcsKeyStoreProvider ks) {
                try { pub = ks.loadPublicKey(KEY_ALIAS); }
                catch (Exception e) {
                    log.warn("Cert lookup failed – deriving pub-key: {}", e.getMessage());
                    pub = CryptoUtils.derivePublicKey(priv);
                }
            } else {
                pub = CryptoUtils.derivePublicKey(priv);
            }
            log.info("🔑 Loaded existing wallet");
            return new Wallet(priv, pub);
        }

        Wallet fresh = new Wallet();
        persist(fresh);
        log.info("🆕 Generated new wallet");
        return fresh;
    }

    private void persist(Wallet w) {
        try { store.save(KEY_ALIAS, w.getPrivateKey()); }
        catch (GeneralSecurityException e) {
            throw new RuntimeException("Failed to save wallet", e);
        }
    }

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
