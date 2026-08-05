package it.uninsubria.laboratoriob.objects;


import it.uninsubria.laboratoriob.Entity;
import it.uninsubria.laboratoriob.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe astratta che rappresenta un utente del sistema.
 * <p>
 * Contiene i dati base dell'utente quali username, nome, cognome, data di
 * nascita e posizione.
 * <p>
 * Gestisce la sicurezza della password tramite hashing e salting.
 * <p>
 * Estende {@link Entity}. Persistenza gestita dai relativi it.uninsubria.laboratoriob.data.DAO
 * ({@code ClientDAO}, {@code OwnerDAO}).
 * <p>
 *
 * @author Luca Cavagnari
 * @version 2.0
 */
@Getter
@Setter
public abstract class User extends Entity {

    private String name;
    private String lastName;
    private Location location;
    private String username;
    private LocalDate dateOfBirth;

    private String passwordHash;
    private String passwordSalt;

    public User(String username, String passwordHash, String salt, String name, String lastName, Location location,
                LocalDate dateOfBirth) {
        super();
        this.username = (username == null || username.length() < 4) ? "user" : username;
        this.name = (name == null || name.length() < 4) ? "nome" : name;
        this.lastName = (lastName == null || lastName.length() < 4) ? "nome" : lastName;
        this.location = location;
        this.dateOfBirth = dateOfBirth;

        this.passwordHash = passwordHash;
        this.passwordSalt = salt;

    }

    public User(UUID id, String username, String passwordHash, String salt, String name, String lastName, Location location,
                LocalDate dateOfBirth) {
        super(id);
        this.username = (username == null || username.length() < 3) ? null : username;
        this.name = (name == null || name.length() < 4) ? "nome" : name;
        this.lastName = (lastName == null || lastName.length() < 4) ? null : lastName;

        this.passwordHash = passwordHash;
        this.passwordSalt = salt;

        this.location = location;

        LocalDate birth = (dateOfBirth == null) ? LocalDate.now().minusDays(1) : dateOfBirth;
        if (birth.isBefore(LocalDate.MIN) || birth.isAfter(LocalDate.now().plusDays(1))) {
            birth = LocalDate.now().minusDays(1);
        }

        this.dateOfBirth = birth;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(lastName, user.lastName) && Objects.equals(location, user.location) && Objects.equals(username, user.username) && Objects.equals(dateOfBirth, user.dateOfBirth) && Objects.equals(passwordHash, user.passwordHash) && Objects.equals(passwordSalt, user.passwordSalt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, lastName, location, username, dateOfBirth, passwordHash, passwordSalt);
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dateOfBirth=" + dateOfBirth;
    }

    public abstract UserRole getRole();

}