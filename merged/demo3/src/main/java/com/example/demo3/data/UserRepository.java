package com.example.demo3.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Legge/scrive gli utenti (Customer/Owner di common-api) come file JSON in data/users/.
 * <p>
 * Le classi di common-api non sono pensate per essere serializzate direttamente da
 * Jackson (campi immutabili, gerarchia astratta, niente costruttore vuoto): la
 * conversione da/verso JSON viene quindi fatta a mano qui, così common-api resta
 * intoccata.
 * <p>
 * Questa classe è una soluzione ponte: quando il modulo server sarà pronto,
 * andrà sostituita da un client che parla col server (es. via socket, riusando
 * {@code it.uninsubria.laboratoriob.api.utils.HeartbeatChannel} per il canale),
 * mantenendo però la stessa interfaccia pubblica in modo che i controller della
 * GUI non debbano cambiare.
 */
public class UserRepository {

    private static final File USERS_DIR = new File("data/users");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public boolean existsByUsername(String username) {
        return userFile(username).exists();
    }

    public Optional<User> findByUsername(String username) {
        File file = userFile(username);
        if (!file.exists()) {
            return Optional.empty();
        }
        try {
            JsonNode node = MAPPER.readTree(file);
            return Optional.of(fromJson(node));
        } catch (IOException e) {
            System.err.println("Errore leggendo l'utente " + username + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean save(User user) {
        if (!USERS_DIR.exists()) {
            USERS_DIR.mkdirs();
        }
        File file = userFile(user.getUsername());
        try {
            MAPPER.writeValue(file, toJson(user));
            return true;
        } catch (IOException e) {
            System.err.println("Errore salvando l'utente " + user.getUsername() + ": " + e.getMessage());
            return false;
        }
    }

    private File userFile(String username) {
        return new File(USERS_DIR, "user_" + username + ".json");
    }

    // ---- conversione manuale User <-> JSON ----

    private ObjectNode toJson(User user) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("id", user.getId().toString());
        node.put("username", user.getUsername());
        node.put("role", user.getRole().name());
        node.put("name", user.getName());
        node.put("lastName", user.getLastName());
        node.put("dateOfBirth", user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        node.put("passwordHash", user.getPasswordHash());
        node.put("passwordSalt", user.getPasswordSalt());

        Location loc = user.getLocation();
        if (loc != null) {
            ObjectNode locNode = node.putObject("location");
            locNode.put("nation", loc.getNation() != null ? loc.getNation().name() : null);
            locNode.put("city", loc.getCity());
            locNode.put("address", loc.getAddress());
            locNode.put("latitude", loc.getLatitude());
            locNode.put("longitude", loc.getLongitude());
        }
        return node;
    }

    private User fromJson(JsonNode node) {
        UUID id = UUID.fromString(node.get("id").asText());
        String username = node.path("username").asText(null);
        String name = node.path("name").asText(null);
        String lastName = node.path("lastName").asText(null);
        String passwordHash = node.path("passwordHash").asText(null);
        String passwordSalt = node.path("passwordSalt").asText(null);

        LocalDate dateOfBirth = node.hasNonNull("dateOfBirth")
                ? LocalDate.parse(node.get("dateOfBirth").asText())
                : null;

        Location location = null;
        JsonNode locNode = node.get("location");
        if (locNode != null && !locNode.isNull()) {
            Nation nation = Nation.fromString(locNode.path("nation").asText(null));
            location = new Location(
                    nation,
                    locNode.path("city").asText(null),
                    locNode.path("latitude").asDouble(0.0),
                    locNode.path("longitude").asDouble(0.0),
                    locNode.path("address").asText(null));
        }

        UserRole role = UserRole.valueOf(node.path("role").asText(UserRole.CLIENT.name()));

        if (role == UserRole.OWNER) {
            return new Owner(id, username, passwordHash, passwordSalt, name, lastName, location, dateOfBirth);
        }
        return new Customer(id, username, passwordHash, passwordSalt, name, lastName, location, dateOfBirth);
    }
}
