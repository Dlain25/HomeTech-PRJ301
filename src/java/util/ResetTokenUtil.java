package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class ResetTokenUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private ResetTokenUtil() { }
    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public static boolean validFormat(String token) {
        return token != null && token.matches("[A-Za-z0-9_-]{43}");
    }
    public static String sha256(String token) {
        if (!validFormat(token)) throw new IllegalArgumentException("Invalid reset token");
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
