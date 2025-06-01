package blockchain.core.crypto;
final class Base58 {
    // Alphabet identical to Bitcoin
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int[]  INDEXES  = new int[128];
    static { java.util.Arrays.fill(INDEXES, -1);
             for (int i = 0; i < ALPHABET.length; i++) INDEXES[ALPHABET[i]] = i; }

    static String encode(byte[] input) {                     // shortest possible impl.
        if (input.length == 0) return "";
        java.math.BigInteger bi = new java.math.BigInteger(1, input);
        StringBuilder sb = new StringBuilder();
        while (bi.compareTo(java.math.BigInteger.ZERO) > 0) {
            java.math.BigInteger[] divRem = bi.divideAndRemainder(java.math.BigInteger.valueOf(58));
            sb.append(ALPHABET[divRem[1].intValue()]);
            bi = divRem[0];
        }
        // keep leading zeros
        for (byte b : input) if (b == 0) sb.append(ALPHABET[0]); else break;
        return sb.reverse().toString();
    }

    static byte[] decode(String s) {
        java.math.BigInteger bi = java.math.BigInteger.ZERO;
        for (char c : s.toCharArray()) {
            int idx = (c < 128) ? INDEXES[c] : -1;
            if (idx < 0) throw new IllegalArgumentException("invalid Base58");
            bi = bi.multiply(java.math.BigInteger.valueOf(58)).add(java.math.BigInteger.valueOf(idx));
        }
        byte[] bytes = bi.toByteArray();
        // cut sign byte
        if (bytes.length > 1 && bytes[0] == 0) bytes = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
        // restore leading zeros
        int leading = 0; for (char c : s.toCharArray()) if (c == ALPHABET[0]) leading++; else break;
        byte[] out = new byte[leading + bytes.length];
        System.arraycopy(bytes, 0, out, leading, bytes.length);
        return out;
    }
}
