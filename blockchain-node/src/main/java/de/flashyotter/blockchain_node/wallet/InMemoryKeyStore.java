// blockchain-node/src/main/java/de/flashyotter/blockchain_node/wallet/InMemoryKeyStore.java
package de.flashyotter.blockchain_node.wallet;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight key store used in tests and by default at runtime.
 * If you want on-disk persistence, start the node with
 * --spring.profiles.active=file-store to switch to {@link FileSystemKeyStore}.
 */
@Component               // registers the bean
@Profile("test")
public class InMemoryKeyStore implements KeyStoreProvider {

    private final Map<String, PrivateKey> keys = new ConcurrentHashMap<>();

    @Override
    public Optional<PrivateKey> load(String alias) {
        return Optional.ofNullable(keys.get(alias));
    }

    @Override
    public void save(String alias, PrivateKey key) throws GeneralSecurityException {
        keys.put(alias, key);
    }
}
