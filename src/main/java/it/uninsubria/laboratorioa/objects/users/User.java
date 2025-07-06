package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratorioa.objects.JsonEntity;
import it.uninsubria.laboratorioa.objects.Location;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Getter
@Setter
public abstract class User extends JsonEntity {

    @Getter(AccessLevel.NONE)
    private final PasswordHasher hasher;
    private String name;
    private String lastName;

    private Location location;

    private String username;
    private LocalDate dateOfBirth;

    public User(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super("users");
        this.username = (username == null || username.length() < 4) ? "user" : username;
        this.name = (name == null || name.length() < 4) ? "nome" : name;
        this.lastName = (lastName == null || lastName.length() < 4) ? "nome" : lastName;
        this.hasher = new PasswordHasher(password);

        this.location = location;

        /*LocalDate birth = (dateOfBirth == null) ? LocalDate.now() : dateOfBirth;
        if (dateOfBirth.isBefore(LocalDate.MIN) || dateOfBirth.isAfter(LocalDate.now().plusDays(1))) {
            birth = LocalDate.MIN;
        }

         */

        //this.dateOfBirth = birth;
        this.dateOfBirth = dateOfBirth;
        build();
    }

    public User(String username, String name, String lastName, Location location, LocalDate dateOfBirth, String password, String salt) {
        this.username = (username == null || username.length() < 4) ? "user" : username.substring(0, 16);
        this.name = (name == null || name.length() < 4) ? "nome" : name.substring(0, 20);
        this.lastName = (lastName == null || lastName.length() < 4) ? "nome" : lastName.substring(0, 24);
        this.hasher = new PasswordHasher(password,salt);

        this.location = location;

        LocalDate birth = (dateOfBirth == null) ? LocalDate.MIN : dateOfBirth;
        if (dateOfBirth.isBefore(LocalDate.MIN) || dateOfBirth.isAfter(LocalDate.now().plusDays(1))) {
            birth = LocalDate.MIN;
        }

        this.dateOfBirth = birth;
        build();
    }

    @Override
    protected void build() {
        jsonObject.put("id", String.valueOf(getId()))
                .put("username", username)
                .put("name", name)
                .put("lastName", lastName);

        if (location != null) jsonObject.set("location", location.getJsonObject());

        ObjectNode password = mapper.createObjectNode()
                .put("salt", hasher.salt)
                .put("password", hasher.pHash);

        jsonObject.set("password", password);
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
        return  "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", password=" + hasher.pHash;
    }

    final static class PasswordHasher {
        private static final int SALT_LENGTH = 16;
        private static final int ITERATIONS = 1000;
        private static final int KEY_LENGTH = 256;

        private final String salt;
        private final String pHash;

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
}
