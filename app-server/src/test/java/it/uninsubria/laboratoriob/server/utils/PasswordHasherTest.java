package it.uninsubria.laboratoriob.server.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordHasher")
class PasswordHasherTest {

    @Test
    @DisplayName("hash() of the same password with the same salt is deterministic")
    void hash_sameSaltSamePassword_isDeterministic() {
        String salt = PasswordHasher.generateSalt();
        assertEquals(PasswordHasher.hash("Sup3rSecret!", salt), PasswordHasher.hash("Sup3rSecret!", salt));
    }

    @Test
    @DisplayName("hash() of the same password with different salts produces different hashes")
    void hash_differentSalts_produceDifferentHashes() {
        String hashA = PasswordHasher.hash("Sup3rSecret!", PasswordHasher.generateSalt());
        String hashB = PasswordHasher.hash("Sup3rSecret!", PasswordHasher.generateSalt());
        assertNotEquals(hashA, hashB);
    }

    @Test
    @DisplayName("generateSalt() never repeats across calls")
    void generateSalt_isRandomAcrossCalls() {
        assertNotEquals(PasswordHasher.generateSalt(), PasswordHasher.generateSalt());
    }

    @Test
    @DisplayName("verify() accepts the correct password")
    void verify_correctPassword_returnsTrue() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash("Sup3rSecret!", salt);
        assertTrue(PasswordHasher.verify("Sup3rSecret!", salt, hash));
    }

    @Test
    @DisplayName("verify() rejects a wrong password")
    void verify_wrongPassword_returnsFalse() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash("Sup3rSecret!", salt);
        assertFalse(PasswordHasher.verify("wrong-password", salt, hash));
    }

    @Test
    @DisplayName("verify() rejects null attempt, salt, or hash instead of throwing")
    void verify_nullArguments_returnsFalseNotThrows() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash("Sup3rSecret!", salt);

        assertFalse(PasswordHasher.verify(null, salt, hash));
        assertFalse(PasswordHasher.verify("Sup3rSecret!", null, hash));
        assertFalse(PasswordHasher.verify("Sup3rSecret!", salt, null));
    }

    @Test
    @DisplayName("hash() rejects a null salt")
    void hash_nullSalt_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash("Sup3rSecret!", null));
    }

    @Test
    @DisplayName("hash() accepts a null password as an empty password rather than throwing")
    void hash_nullPassword_doesNotThrow() {
        String salt = PasswordHasher.generateSalt();
        assertDoesNotThrow(() -> PasswordHasher.hash(null, salt));
    }
}
