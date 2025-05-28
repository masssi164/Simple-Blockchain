package blockchain.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;     // ← ensure this import
import java.util.Base64;

import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

/** EC helpers for (de)serialisation and ECDSA signing/verification. */
public final class CryptoUtils {

    static {
        // register BC so secp256k1 is available to both sign & verify
        Security.addProvider(new BouncyCastleProvider());
    }

    private CryptoUtils() { }

    /* ----------- ECDSA ----------- */

    public static byte[] applyEcdsaSignature(PrivateKey privKey, String msg) {
        try {
            // explicit BC provider
            Signature signer = Signature.getInstance(
                "SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
            signer.initSign(privKey);
            signer.update(msg.getBytes(StandardCharsets.UTF_8));
            return signer.sign();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("ECDSA signing failed", e);
        }
    }

    public static boolean verifyEcdsaSignature(PublicKey pubKey,
                                               String msg,
                                               byte[] sigBytes) {
        try {
            // same provider for verification
            Signature verifier = Signature.getInstance(
                "SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
            verifier.initVerify(pubKey);
            verifier.update(msg.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(sigBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("ECDSA verification failed", e);
        }
    }

    /* -------- key (de)serialisation -------- */

    public static String keyToBase64(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey publicKeyFromBase64(String b64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            // use BC so curve params are picked up
            KeyFactory kf = KeyFactory.getInstance(
                "EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("PublicKey deserialization failed", e);
        }
    }


    /**
     * Derive the public key from the private key (EC) by doing G·d.
     */
    public static PublicKey derivePublicKey(PrivateKey privKey) {
        try {
            // cast to BC’s ECPrivateKey
            ECPrivateKey bcPriv = (ECPrivateKey) privKey;
            // pull out the curve parameters
            ECParameterSpec bcSpec = bcPriv.getParameters();
            // compute Q = G · d
            ECPoint q = bcSpec.getG().multiply(bcPriv.getD());
            // build a BC public‐key spec
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(q, bcSpec);
            // ask BC’s KeyFactory to turn that into a java.security.PublicKey
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePublic(pubSpec);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("PublicKey derivation failed", e);
        }
    }
}    
