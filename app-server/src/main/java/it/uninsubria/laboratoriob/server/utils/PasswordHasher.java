package it.uninsubria.laboratoriob.server.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Classe di utilità per l'hashing e la verifica delle password.
 * <p>
 * Utilizza l'algoritmo PBKDF2 con HMAC-SHA256 per generare hash sicuri delle password.
 * Ogni hash è accompagnato da un salt generato casualmente per prevenire attacchi rainbow table.
 * </p>
 *
 * <h2>Parametri di sicurezza</h2>
 * <ul>
 *   <li>Lunghezza salt: 16 byte</li>
 *   <li>Iterazioni PBKDF2: 10.000</li>
 *   <li>Lunghezza chiave derivata: 256 bit</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see javax.crypto.SecretKeyFactory
 */
public final class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10_000;
    private static final int KEY_LENGTH = 256;

    private PasswordHasher() {
        throw new AssertionError("Utility class");
    }

    /**
     * Genera un salt casuale per l'hashing della password.
     *
     * @return stringa Base64 contenente il salt generato
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Calcola l'hash della password utilizzando l'algoritmo PBKDF2WithHmacSHA256.
     *
     * @param password password in chiaro da hashare
     * @param salt     salt Base64 utilizzato per l'hashing
     * @return hash della password in formato Base64
     * @throws IllegalArgumentException se il salt è nullo
     */
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

    /**
     * Verifica se una password tentata corrisponde all'hash atteso.
     *
     * @param attempt    password in chiaro da verificare
     * @param salt       salt Base64 utilizzato durante la creazione dell'hash
     * @param expectedHash hash atteso da confrontare
     * @return {@code true} se la password corrisponde, {@code false} altrimenti
     */
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