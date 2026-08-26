package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione JSON del DAO per l'entità {@link Location}.
 * <p>
 * Gestisce le operazioni CRUD sulle posizioni geografiche usando file JSON come storage locale.
 * Le location sono identificate dalla coppia (latitude, longitude) anziché da UUID.
 * </p>
 */
public final class JsonLocationDAO implements DAO<Location> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;

    public JsonLocationDAO() {
        this.storeFile = new File(Constants.ROOT, "locations.json");
    }

    private ArrayNode loadAll() {
        if (!storeFile.exists()) return mapper.createArrayNode();
        try {
            JsonNode node = mapper.readTree(storeFile);
            return node.isArray() ? (ArrayNode) node : mapper.createArrayNode();
        } catch (IOException e) {
            System.err.println("Errore loadAll in JsonLocationDAO: " + e.getMessage());
            return mapper.createArrayNode();
        }
    }

    private void persist(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(storeFile, array);
        } catch (IOException e) {
            System.err.println("Errore persist in JsonLocationDAO: " + e.getMessage());
        }
    }

    private Location mapNode(JsonNode node) {
        return new Location(
                Nation.fromString(node.path("nation").asText()),
                node.path("city").asText(),
                node.path("latitude").asDouble(),
                node.path("longitude").asDouble(),
                node.path("address").asText()
        );
    }

    private ObjectNode toNode(Location loc) {
        ObjectNode node = mapper.createObjectNode();
        node.put("nation", loc.getNation() != null ? loc.getNation().name() : "");
        node.put("city", loc.getCity());
        node.put("latitude", loc.getLatitude());
        node.put("longitude", loc.getLongitude());
        node.put("address", loc.getAddress());
        return node;
    }

    public Optional<Location> findByCoordinates(double lat, double lon) {
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (Math.abs(node.path("latitude").asDouble() - lat) < 1e-6
                    && Math.abs(node.path("longitude").asDouble() - lon) < 1e-6) {
                return Optional.of(mapNode(node));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Location> findAll() {
        ArrayNode array = loadAll();
        List<Location> locations = new ArrayList<>();
        for (JsonNode node : array) {
            locations.add(mapNode(node));
        }
        return locations;
    }

    @Override
    public boolean save(Location location) {
        if (location == null) return false;
        ArrayNode array = loadAll();

        for (JsonNode node : array) {
            if (Math.abs(node.path("latitude").asDouble() - location.getLatitude()) < 1e-6
                    && Math.abs(node.path("longitude").asDouble() - location.getLongitude()) < 1e-6) {
                return false;
            }
        }

        array.add(toNode(location));
        persist(array);
        return true;
    }

    @Override
    public boolean update(Location location) {
        if (location == null) return false;
        ArrayNode array = loadAll();

        for (int i = 0; i < array.size(); i++) {
            JsonNode node = array.get(i);
            if (Math.abs(node.path("latitude").asDouble() - location.getLatitude()) < 1e-6
                    && Math.abs(node.path("longitude").asDouble() - location.getLongitude()) < 1e-6) {
                array.set(i, toNode(location));
                persist(array);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        return false;
    }

    public boolean deleteByCoordinates(double lat, double lon) {
        ArrayNode array = loadAll();
        for (int i = 0; i < array.size(); i++) {
            JsonNode node = array.get(i);
            if (Math.abs(node.path("latitude").asDouble() - lat) < 1e-6
                    && Math.abs(node.path("longitude").asDouble() - lon) < 1e-6) {
                array.remove(i);
                persist(array);
                return true;
            }
        }
        return false;
    }

    // Location has no ID attribute
    @Override public Optional<Location> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<Location> findAll(int offset, int limit) {
        List<Location> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() {
        return loadAll().size();
    }
}
