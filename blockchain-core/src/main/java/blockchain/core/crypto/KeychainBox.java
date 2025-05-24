package blockchain.core.crypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

/**
 * Minimal wrapper around the macOS “security” CLI so we don’t have to
 * deal with the internal, unsupported <em>KeychainStore</em> provider.
 *
 * The private key is stored as a **base64 PKCS#8 blob** in the user’s
 * login keychain.  Nothing is encrypted here – the keychain itself is
 * already encrypted and protected by the macOS account password / Touch-ID.
 */
public final class KeychainBox {

    private static final boolean MAC = System.getProperty("os.name")
                                             .toLowerCase()
                                             .contains("mac");

    private KeychainBox() { }

    /** Returns an empty {@code Optional} on non-macOS or if the item is absent. */
    public static Optional<PrivateKey> load(String alias) {
        if (!MAC) return Optional.empty();

        try {
            Process p = new ProcessBuilder(
                    "security", "find-generic-password",
                    "-a", alias, "-s", alias, "-w")      // -w = password only on stdout
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();

            byte[] out = p.getInputStream().readAllBytes();
            if (p.waitFor() != 0) return Optional.empty();          // item not found

            String b64 = new String(out).trim();
            byte[] pkcs8 = Base64.getDecoder().decode(b64);
            PrivateKey key = KeyFactory.getInstance("EC")
                                       .generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
            return Optional.of(key);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Adds / replaces the key identified by {@code alias}.  No-op on non-macOS. */
    public static void store(String alias, PrivateKey key) throws Exception {
        if (!MAC) return;   // silently ignore on other OSes

        String b64 = Base64.getEncoder().encodeToString(key.getEncoded());

        Process p = new ProcessBuilder(
                "security", "add-generic-password",
                "-a", alias, "-s", alias,
                "-w", b64,
                "-U")        // -U = update if item already exists
                .inheritIO()
                .start();

        if (p.waitFor() != 0)
            throw new IllegalStateException("security CLI failed (exit " + p.exitValue() + ')');
    }
}
