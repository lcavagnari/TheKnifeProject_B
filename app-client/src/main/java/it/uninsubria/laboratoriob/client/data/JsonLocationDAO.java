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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonLocationDAO implements DAO<Location> {

    // TOOD: ensure this works only as dependency for user and restaurants

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;

    private record LocKey(double lat, double lon) {}
    private final ConcurrentHashMap<LocKey, Location> cache = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonLocationDAO() {
        this.storeFile = new File(Constants.ROOT, "locations.json");
    }

    private void ensureCacheLoaded() {
        if (cacheLoaded) return;
        synchronized (this) {
            if (cacheLoaded) return;
            loadFromDisk();
            cacheLoaded = true;
        }
    }

    private void loadFromDisk() {
        cache.clear();
        if (!storeFile.exists()) return;
        try {
            JsonNode node = mapper.readTree(storeFile);
            if (!node.isArray()) return;
            for (JsonNode n : (ArrayNode) node) {
                Location loc = mapNode(n);
                cache.put(new LocKey(loc.getLatitude(), loc.getLongitude()), loc);
            }
        } catch (IOException e) {
            System.err.println("Errore loadFromDisk in JsonLocationDAO: " + e.getMessage());
        }
    }

    private void persistAtomic(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            File tmp = File.createTempFile("locations_", ".json", storeFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), storeFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistAtomic in JsonLocationDAO: " + e.getMessage());
        }
    }

    private ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Location loc : cache.values()) {
            array.add(toNode(loc));
        }
        return array;
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
        ensureCacheLoaded();
        for (LocKey key : cache.keySet()) {
            if (Math.abs(key.lat() - lat) < 1e-6 && Math.abs(key.lon() - lon) < 1e-6) {
                return Optional.of(cache.get(key));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Location> findAll() {
        ensureCacheLoaded();
        return new ArrayList<>(cache.values());
    }

    @Override
    public boolean save(Location location) {
        if (location == null) return false;
        ensureCacheLoaded();
        LocKey key = new LocKey(location.getLatitude(), location.getLongitude());
        if (cache.containsKey(key)) return false;
        cache.put(key, location);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean update(Location location) {
        if (location == null) return false;
        ensureCacheLoaded();
        LocKey key = new LocKey(location.getLatitude(), location.getLongitude());
        if (!cache.containsKey(key)) return false;
        cache.put(key, location);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        return false;
    }

    public boolean deleteByCoordinates(double lat, double lon) {
        ensureCacheLoaded();
        LocKey key = new LocKey(lat, lon);
        Location removed = cache.remove(key);
        if (removed == null) return false;
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public Optional<Location> findById(UUID id) {
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
        ensureCacheLoaded();
        return cache.size();
    }
}
