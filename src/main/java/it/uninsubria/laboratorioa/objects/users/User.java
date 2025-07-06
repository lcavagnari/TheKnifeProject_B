package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratorioa.objects.JsonEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
public abstract class User extends JsonEntity {

    final static class PasswordHasher {
        private static final int SALT_LENGTH = 16;
        private static final int ITERATIONS = 100_000;
        private static final int KEY_LENGTH = 256;

        private final String salt;
        private String pHash;

        public PasswordHasher(String password) {
            byte[] saltBytes = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(saltBytes);
            this.salt = Base64.getEncoder().encodeToString(saltBytes);

            char[] plainPassword = password.toCharArray();

            this.pHash = run(plainPassword, saltBytes);
            Arrays.fill(plainPassword, '\0');
        }

        public PasswordHasher(String password, String salt) {
            byte[] saltBytes = salt.getBytes();
            this.salt = Base64.getEncoder().encodeToString(saltBytes);

            char[] plainPassword = password.toCharArray();

            this.pHash = run(plainPassword, saltBytes);
            Arrays.fill(plainPassword, '\0');
        }

        private String run(char[] password, byte[] saltBytes) {
            try {
                PBEKeySpec spec = new PBEKeySpec(password, saltBytes, ITERATIONS, KEY_LENGTH);
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

                byte[] hash = skf.generateSecret(spec).getEncoded();
                return Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                throw new RuntimeException("Password hashing failed", e);
            }
        }

        public boolean verify(char[] attempt) {
            if (attempt == null) return false;

            byte[] saltBytes = Base64.getDecoder().decode(salt);

            String attemptHash = run(attempt, saltBytes);
            Arrays.fill(attempt, '\0');
            return Objects.equals(pHash, attemptHash);
        }
    }


    private String name;
    private String lastName;

    private String username;
    private LocalDate dateOfBirth;

    @Getter(AccessLevel.NONE)
    private final PasswordHasher hasher;

    public User(String username, String password, String name, String lastName, LocalDate dateOfBirth) {
        this.username = (username == null || username.length() < 4) ? "user" : username.substring(0,16);
        this.name = (name == null || name.length() < 4) ? "nome" : name.substring(0,20);
        this.lastName = (lastName == null || lastName.length() < 4) ? "nome" : lastName.substring(0,20);
        this.dateOfBirth = dateOfBirth;

        this.hasher = new PasswordHasher(password);

        build();
    }

    @Override
    protected void build() {
        jsonObject.put("id", getId().toString())
                .put("username", username)
                .put("name", name)
                .put("lastName", lastName);

        ObjectNode password = mapper.createObjectNode()
                .put("salt", hasher.salt)
                .put("password", hasher.pHash);

        jsonObject.set("password",password);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(lastName, user.lastName) && Objects.equals(username, user.username) && Objects.equals(dateOfBirth, user.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, lastName, username, dateOfBirth, hasher.salt);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", password=" + hasher.pHash;
    }
}
