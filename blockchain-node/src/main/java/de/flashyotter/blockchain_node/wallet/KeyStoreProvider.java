package de.flashyotter.blockchain_node.wallet;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Optional;

/** Strategy SPI for key storage. */
public interface KeyStoreProvider {

    Optional<PrivateKey> load(String alias) throws GeneralSecurityException;

    void save(String alias, PrivateKey key) throws GeneralSecurityException;
}
