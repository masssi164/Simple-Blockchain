package blockchain.core.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper utilities for EC key (de)serialisation and ECDSA
 * signing / verification.  All cryptographic operations are
 * performed with Bouncy Castle to ensure that the secp256k1
 * curve is available on every JVM.
 */
@Slf4j
public final class CryptoUtils {

    static {
        // Register Bouncy Castle once at class-initialisation time
        Security.addProvider(new BouncyCastleProvider());
    }

    private CryptoUtils() { }

    /* ------------------------------------------------------------------
     *  E C D S A   S I G N I N G
     * ------------------------------------------------------------------ */

    /**
     * Signs {@code msg} with the given private key and returns a
     * canonicalised low-S DER signature.
     *
     * @param privKey  EC private key (secp256k1)
     * @param msg      message to sign (UTF-8)
     * @return DER-encoded ECDSA signature in low-S form
     */
    public static byte[] applyEcdsaSignature(PrivateKey privKey, String msg) {
        try {
            Signature signer = Signature.getInstance(
                    "SHA256withECDSA",
                    BouncyCastleProvider.PROVIDER_NAME);
            signer.initSign(privKey);
            signer.update(msg.getBytes(StandardCharsets.UTF_8));

            byte[] der = signer.sign();                       // R|S (possibly high-S)

            /* ------------------ Low-S normalisation ------------------ */
            ASN1Sequence seq = ASN1Sequence.getInstance(der);
            BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getValue();
            BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getValue();

            BigInteger n = curveOrder(privKey);               // curve order
            if (s.compareTo(n.subtract(s)) > 0) {             // if S > n/2
                s = n.subtract(s);                            //   mirror it
            }

            var canonical = new DERSequence(new ASN1Integer[] {
                    new ASN1Integer(r), new ASN1Integer(s) });

            return canonical.getEncoded();
        } catch (GeneralSecurityException | IOException e) {
            log.error("ECDSA signing failed", e);
            throw new RuntimeException("ECDSA signing failed", e);
        }
    }

    /* ------------------------------------------------------------------
     *  K E Y   ( D E ) S E R I A L I S A T I O N
     * ------------------------------------------------------------------ */

    /**
     * Encodes the supplied public key in raw X.509 format and then
     * Base-64 encodes the result.
     */
    public static String keyToBase64(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Reconstructs an EC public key from the given Base-64 string.
     */
    public static PublicKey publicKeyFromBase64(String b64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            KeyFactory kf = KeyFactory.getInstance(
                    "EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (GeneralSecurityException e) {
            log.error("Public key deserialisation failed", e);
            throw new RuntimeException("Public key deserialisation failed", e);
        }
    }

    /**
     * Derives the public key (Q) from the private scalar (d) by computing
     * Q = G·d on the secp256k1 curve.
     */
    public static PublicKey derivePublicKey(PrivateKey privKey) {
        try {
            ECPrivateKey bcPriv = (ECPrivateKey) privKey;
            ECParameterSpec spec = bcPriv.getParameters();
            ECPoint q = spec.getG().multiply(bcPriv.getD());

            ECPublicKeySpec pubSpec = new ECPublicKeySpec(q, spec);
            KeyFactory kf = KeyFactory.getInstance(
                    "EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePublic(pubSpec);
        } catch (GeneralSecurityException e) {
            log.error("Public key derivation failed", e);
            throw new RuntimeException("Public key derivation failed", e);
        }
    }

    /* ------------------------------------------------------------------
     *  E C D S A   V E R I F I C A T I O N
     * ------------------------------------------------------------------ */

    /**
     * Verifies a DER-encoded ECDSA signature against {@code msg}.
     * Signatures that are not already in low-S form are rejected.
     */
    public static boolean verifyEcdsaSignature(PublicKey pubKey,
                                               String     msg,
                                               byte[]     sigBytes) {
        try {
            // Enforce BIP-62 low-S rule before the expensive cryptographic check
            if (!isLowS(sigBytes, pubKey)) {
                log.debug("Rejected signature with high-S value");
                return false;
            }

            Signature verifier = Signature.getInstance(
                    "SHA256withECDSA",
                    BouncyCastleProvider.PROVIDER_NAME);
            verifier.initVerify(pubKey);
            verifier.update(msg.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(sigBytes);
        } catch (GeneralSecurityException e) {
            log.error("ECDSA verification failed", e);
            throw new RuntimeException("ECDSA verification failed", e);
        }
    }

    /**
     * Checks whether a DER signature is low-S according to BIP-62
     * (i.e. S ≤ n/2).  Invalid or unparsable signatures return {@code false}.
     */
    private static boolean isLowS(byte[] derSig, PublicKey pubKey) {
        try {
            ASN1Sequence seq = ASN1Sequence.getInstance(derSig);
            BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getValue();

            BigInteger n = (pubKey instanceof ECPublicKey ec)
                    ? ec.getParameters().getN()
                    : SECP256K1_N;

            return s.compareTo(n.shiftRight(1)) <= 0;
        } catch (Exception e) {
            return false;  // parsing failed → treat as invalid
        }
    }

    /* ------------------------------------------------------------------
     *  I N T E R N A L   H E L P E R S
     * ------------------------------------------------------------------ */

    /**
     * Returns the curve order n for the supplied private key,
     * regardless of which provider generated it.  Falls back to a
     * hard-coded secp256k1 constant if necessary.
     */
    private static BigInteger curveOrder(PrivateKey key) {
        if (key instanceof org.bouncycastle.jce.interfaces.ECPrivateKey bcKey) {
            return bcKey.getParameters().getN();
        }
        if (key instanceof java.security.interfaces.ECPrivateKey sunKey) {
            return sunKey.getParams().getOrder();
        }
        return SECP256K1_N;
    }

    /** Hard-coded order n of the secp256k1 curve. */
    private static final BigInteger SECP256K1_N =
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
}
