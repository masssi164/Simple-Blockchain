package simple.blockchain.Utils;

import java.security.*;
import java.util.Base64;

public class CryptoUtil {

    public static byte[] applyECDSASig(PrivateKey priv, String msg) {
        try {
            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initSign(priv); s.update(msg.getBytes()); return s.sign();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static boolean verifyECDSASig(PublicKey pub, String msg, byte[] sig) {
        try {
            Signature v = Signature.getInstance("SHA256withECDSA");
            v.initVerify(pub); v.update(msg.getBytes()); return v.verify(sig);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static String keyToString(Key k) {
        return Base64.getEncoder().encodeToString(k.getEncoded());
    }
}
