package it.uninsubria.laboratoriob.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public final class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10_000;
    private static final int KEY_LENGTH = 256;

    private PasswordHasher() {
        throw new AssertionError("Utility class");
    }

    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        if (salt == null)
            throw new IllegalArgumentException("Salt cannot be null");

        byte[] saltBytes = Base64.getDecoder().decode(salt);

        char[] chars = password != null ? password.toCharArray() : new char[0];
        try {
            return run(chars, saltBytes);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    public static boolean verify(String attempt, String salt, String expectedHash) {
        if (attempt == null || salt == null || expectedHash == null)
            return false;

        return Objects.equals(expectedHash, hash(attempt, salt));
    }

    private static String run(char[] password, byte[] saltBytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}