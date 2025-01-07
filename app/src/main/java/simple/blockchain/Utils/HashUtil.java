package simple.blockchain.Utils;

import java.security.MessageDigest;

public class HashUtil {

    public static byte[] applySha256AsBytes(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Error applying SHA-256", e);
        }
    }
}

