package blockchain.core.crypto;

import java.security.*;
import java.util.Base64;

/** Thin ECDSA helper – mirrors Fabric/Geth naming. */
public final class CryptoUtils {

    private CryptoUtils() {}

    /** Sign arbitrary text with SHA-256 / ECDSA. */
    public static byte[] applyEcdsaSignature(PrivateKey priv, String msg) {
        try {
            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initSign(priv);
            s.update(msg.getBytes());
            return s.sign();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /** Verify SHA-256 / ECDSA signature. */
    public static boolean verifyEcdsaSignature(PublicKey pub, String msg, byte[] sig) {
        try {
            Signature v = Signature.getInstance("SHA256withECDSA");
            v.initVerify(pub);
            v.update(msg.getBytes());
            return v.verify(sig);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static String keyToBase64(Key k) {
        return Base64.getEncoder().encodeToString(k.getEncoded());
    }
}
