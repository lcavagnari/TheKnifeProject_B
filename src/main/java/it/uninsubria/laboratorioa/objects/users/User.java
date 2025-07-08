package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratorioa.objects.JsonEntity;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.utils.IO;
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

/**
 * Classe astratta che rappresenta un utente del sistema.<p>
 * Contiene i dati base dell'utente quali username, nome, cognome, data di nascita e posizione.<p>
 * Gestisce la sicurezza della password tramite hashing e salting.<p>
 * Estende {@link JsonEntity} per la serializzazione JSON.<p>
 * <p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
@Getter
@Setter
public abstract class User extends JsonEntity {

    /**
     * Gestore dell'hash della password e del salt.<p>
     * Non esposto con getter per sicurezza.
     */
    @Getter(AccessLevel.NONE)
    private final PasswordHasher hasher;

    /**
     * Nome dell'utente.
     */
    private String name;

    /**
     * Cognome dell'utente.
     */
    private String lastName;

    /**
     * Posizione geografica dell'utente.
     */
    private Location location;

    /**
     * Nome utente per il login.
     */
    private String username;

    /**
     * Data di nascita dell'utente.
     */
    private LocalDate dateOfBirth;

    /**
     * Costruttore che inizializza un utente con password in chiaro.<p>
     * Esegue validazioni di base su username, nome e cognome.<p>
     * Costruisce l'oggetto JSON.<p>
     *
     * @param username    nome utente
     * @param password    password in chiaro
     * @param name        nome
     * @param lastName    cognome
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     */
    public User(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super("users");
        this.username = (username == null || username.length() < 4) ? "user" : username;
        this.name = (name == null || name.length() < 4) ? "nome" : name;
        this.lastName = (lastName == null || lastName.length() < 4) ? "nome" : lastName;
        this.hasher = new PasswordHasher(password);

        this.location = location;

        /* Controllo data di nascita eventualmente da riabilitare in futuro
         * LocalDate birth = (dateOfBirth == null) ? LocalDate.now() : dateOfBirth;
         * if (dateOfBirth.isBefore(LocalDate.MIN) || dateOfBirth.isAfter(LocalDate.now().plusDays(1))) {
         *     birth = LocalDate.MIN;
         * }
         */
        //this.dateOfBirth = birth;
        this.dateOfBirth = dateOfBirth;

        IO.validateUser(this);
        build();
    }

    /**
     * Costruttore con password hashata e salt forniti.<p>
     * Limita la lunghezza di username, nome e cognome.<p>
     * Valida data di nascita.<p>
     * Costruisce l'oggetto JSON.<p>
     *
     * @param username    nome utente
     * @param name        nome
     * @param lastName    cognome
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     * @param password    password hashata
     * @param salt        salt usato nell'hash
     */
    public User(String username, String name, String lastName, Location location, LocalDate dateOfBirth, String password, String salt) {
        this.username = (username == null || username.length() < 3) ? null : username.substring(0, 16);
        this.name = (name == null || name.length() < 4) ? "nome" : name.substring(0, 20);
        this.lastName = (lastName == null || lastName.length() < 4) ? null : lastName.substring(0, 24);

        if (salt != null) this.hasher = new PasswordHasher(password, salt);
        else this.hasher = new PasswordHasher(password);

        this.location = location;

        LocalDate birth = (dateOfBirth == null) ? LocalDate.MIN : dateOfBirth;
        if (dateOfBirth.isBefore(LocalDate.MIN) || dateOfBirth.isAfter(LocalDate.now().plusDays(1))) {
            birth = LocalDate.MIN;
        }

        this.dateOfBirth = birth;

        build();
    }

    /**
     * Verifica se la password fornita corrisponde all'hash salvato.<p>
     * Restituisce false se la password è nulla o troppo corta.<p>
     *
     * @param other password da verificare
     * @return true se la password è corretta, false altrimenti
     */
    public boolean verifyPassword(String other) {
        if (other == null || other.length() < 8) return false;
        else return hasher.verify(other.toCharArray());
    }

    /**
     * Costruisce la rappresentazione JSON dell'utente.<p>
     * Inserisce id, username, nome, cognome, data di nascita e posizione.<p>
     * Inserisce anche la password hashata e il salt.<p>
     */
    @Override
    protected void build() {
        jsonObject.put("id", String.valueOf(getId()))
                .put("username", username)
                .put("name", name)
                .put("lastName", lastName);

        jsonObject.put("dateOfBirth", dateOfBirth.toString());
        if (location != null) jsonObject.set("location", location.getJsonObject());

        ObjectNode password = mapper.createObjectNode()
                .put("salt", hasher.salt)
                .put("password", hasher.pHash);

        jsonObject.set("password", password);
    }

    /**
     * Confronta l'utente con un altro oggetto per uguaglianza.<p>
     * Controlla anche i campi nome, cognome, username e data di nascita.<p>
     *
     * @param o oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(lastName, user.lastName) && Objects.equals(username, user.username) && Objects.equals(dateOfBirth, user.dateOfBirth);
    }

    /**
     * Calcola l'hash code dell'utente.<p>
     * Usa i campi base e il salt della password.<p>
     *
     * @return hash code calcolato
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, lastName, username, dateOfBirth, hasher.salt);
    }

    /**
     * Rappresentazione testuale dell'utente.<p>
     * Mostra id, nome, cognome, username, data di nascita e hash della password.<p>
     *
     * @return stringa rappresentativa dell'utente
     */
    @Override
    public String toString() {
        return "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", password=" + hasher.pHash;
    }

    /**
     * Classe statica interna per gestire l'hashing e la verifica della password.<p>
     * Usa algoritmo PBKDF2WithHmacSHA256 con salt e iterazioni.<p>
     */
    final static class PasswordHasher {
        private static final int SALT_LENGTH = 16;
        private static final int ITERATIONS = 10000;
        private static final int KEY_LENGTH = 256;

        /**
         * Salt codificato in Base64.
         */
        private final String salt;

        /**
         * Hash della password codificato in Base64.
         */
        private final String pHash;

        /**
         * Costruttore che genera un nuovo salt casuale e calcola l'hash della password.<p>
         *
         * @param password password in chiaro
         */
        public PasswordHasher(String password) {
            byte[] saltBytes = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(saltBytes);
            this.salt = Base64.getEncoder().encodeToString(saltBytes);

            char[] plainPassword = password.toCharArray();

            this.pHash = run(plainPassword, saltBytes);
            Arrays.fill(plainPassword, '\0');
        }

        /**
         * Costruttore che riceve un salt esistente e calcola l'hash della password.<p>
         *
         * @param password password in chiaro
         * @param salt     salt esistente in formato stringa
         */
        public PasswordHasher(String password, String salt) {
            byte[] saltBytes = salt.getBytes();
            this.salt = Base64.getEncoder().encodeToString(saltBytes);

            char[] plainPassword = password.toCharArray();

            this.pHash = run(plainPassword, saltBytes);
            Arrays.fill(plainPassword, '\0');
        }

        /**
         * Esegue l'hashing della password usando PBKDF2 con il salt e iterazioni.<p>
         *
         * @param password  password in char array
         * @param saltBytes salt in byte array
         * @return hash codificato in Base64
         */
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

        /**
         * Verifica se un tentativo di password corrisponde all'hash salvato.<p>
         * Pulisce l'array di tentativo dopo il confronto.<p>
         *
         * @param attempt password in char array da verificare
         * @return true se corrisponde, false altrimenti
         */
        public boolean verify(char[] attempt) {
            if (attempt == null) return false;

            byte[] saltBytes = Base64.getDecoder().decode(salt);

            String attemptHash = run(attempt, saltBytes);
            Arrays.fill(attempt, '\0');

            return Objects.equals(pHash, attemptHash);
        }
    }
}
