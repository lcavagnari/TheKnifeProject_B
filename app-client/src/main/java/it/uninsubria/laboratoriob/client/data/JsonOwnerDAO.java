package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Implementazione JSON del DAO per l'entità {@link Owner}.
 * <p>
 * Gestisce le operazioni CRUD sui proprietari usando file JSON come storage locale.
 * </p>
 */
public final class JsonOwnerDAO implements DAO<Owner> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;
    private final AuthServiceInter service;

    public JsonOwnerDAO(AuthServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "owners.json");
        this.service = service;
    }

    private ArrayNode loadAll() {
        if (!storeFile.exists()) return mapper.createArrayNode();
        try {
            JsonNode node = mapper.readTree(storeFile);
            return node.isArray() ? (ArrayNode) node : mapper.createArrayNode();
        } catch (IOException e) {
            System.err.println("Errore loadAll in JsonOwnerDAO: " + e.getMessage());
            return mapper.createArrayNode();
        }
    }

    private void persist(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(storeFile, array);
        } catch (IOException e) {
            System.err.println("Errore persist in JsonOwnerDAO: " + e.getMessage());
        }
    }

    private Owner mapNode(JsonNode node) {
        UUID id = UUID.fromString(node.path("id").asText());
        String username = node.path("username").asText();
        String passwordHash = node.path("passwordHash").asText();
        String passwordSalt = node.path("passwordSalt").asText();
        String name = node.path("name").asText();
        String lastName = node.path("lastName").asText();
        LocalDate dateOfBirth = LocalDate.parse(node.path("dateOfBirth").asText());
        boolean system = node.path("system").asBoolean(false);

        Location loc = null;
        JsonNode locNode = node.path("location");
        if (!locNode.isMissingNode() && !locNode.isNull()) {
            loc = new Location(
                    Nation.fromString(locNode.path("nation").asText()),
                    locNode.path("city").asText(),
                    locNode.path("latitude").asDouble(),
                    locNode.path("longitude").asDouble(),
                    locNode.path("address").asText()
            );
        }

        Set<UUID> restaurantIds = new HashSet<>();
        JsonNode restNode = node.path("restaurantIds");
        if (restNode.isArray()) {
            for (JsonNode r : restNode) {
                restaurantIds.add(UUID.fromString(r.asText()));
            }
        }

        return new Owner(id, username, passwordHash, passwordSalt, name, lastName, loc, dateOfBirth, system);
    }

    private ObjectNode toNode(Owner owner) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", owner.getId().toString());
        node.put("username", owner.getUsername());
        node.put("passwordHash", owner.getPasswordHash());
        node.put("passwordSalt", owner.getPasswordSalt());
        node.put("name", owner.getName());
        node.put("lastName", owner.getLastName());
        node.put("dateOfBirth", owner.getDateOfBirth().toString());
        node.put("system", owner.isSystem());

        if (owner.getLocation() != null) {
            ObjectNode locNode = mapper.createObjectNode();
            locNode.put("nation", owner.getLocation().getNation().name());
            locNode.put("city", owner.getLocation().getCity());
            locNode.put("latitude", owner.getLocation().getLatitude());
            locNode.put("longitude", owner.getLocation().getLongitude());
            locNode.put("address", owner.getLocation().getAddress());
            node.set("location", locNode);
        }

        ArrayNode restArray = mapper.createArrayNode();
        owner.getRestaurantsById().keySet().forEach(id -> restArray.add(id.toString()));
        node.set("restaurantIds", restArray);

        return node;
    }

    @Override
    public Optional<Owner> findById(UUID id) {
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (node.path("id").asText().equals(id.toString())) {
                return Optional.of(mapNode(node));
            }
        }
        return Optional.empty();
    }

    public Optional<Owner> findByUsername(String username) {
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (node.path("username").asText().equals(username)) {
                return Optional.of(mapNode(node));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Owner> findAll() {
        ArrayNode array = loadAll();
        List<Owner> owners = new ArrayList<>();
        for (JsonNode node : array) {
            owners.add(mapNode(node));
        }
        return owners;
    }

    @Override
    public List<Owner> findAll(int offset, int limit) {
        List<Owner> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() {
        return loadAll().size();
    }

    @Override
    public boolean save(Owner owner) {
        if (owner == null) return false;
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (node.path("id").asText().equals(owner.getId().toString())) {
                return false;
            }
        }
        array.add(toNode(owner));
        persist(array);
        return true;
    }

    @Override
    public boolean update(Owner owner) {
        if (owner == null) return false;
        ArrayNode array = loadAll();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).path("id").asText().equals(owner.getId().toString())) {
                array.set(i, toNode(owner));
                persist(array);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        ArrayNode array = loadAll();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).path("id").asText().equals(id.toString())) {
                array.remove(i);
                persist(array);
                return true;
            }
        }
        return false;
    }
}
