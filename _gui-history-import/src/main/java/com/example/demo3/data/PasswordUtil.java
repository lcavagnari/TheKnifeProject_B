package com.example.demo3.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashing password con SHA-256 + salt casuale.
 * <p>
 * NOTA: questa è un'implementazione temporanea lato client, usata finché
 * non esiste un server che gestisce l'autenticazione. Quando il modulo
 * server sarà disponibile, l'hashing andrà spostato lì (il client non
 * dovrebbe mai fidarsi di sé stesso per la sicurezza delle credenziali) e
 * qui rimarrà solo l'invio della password in chiaro su connessione sicura.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo di hashing non disponibile", e);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        return hash(password, salt).equals(expectedHash);
    }
}
