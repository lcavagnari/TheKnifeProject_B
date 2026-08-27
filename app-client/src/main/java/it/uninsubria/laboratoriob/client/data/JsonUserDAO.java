package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class JsonUserDAO<T extends User> implements DAO<T> {

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected final File storeFile;
    protected final ConcurrentHashMap<UUID, T> cacheById = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, T> cacheByUsername = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    protected JsonUserDAO(String fileName) {
        this.storeFile = new File(Constants.ROOT, fileName);
    }

    protected abstract T mapNode(JsonNode node);
    protected abstract ArrayNode toArrayNode();

    private void ensureCacheLoaded() {
        if (cacheLoaded) return;
        synchronized (this) {
            if (cacheLoaded) return;
            loadFromDisk();
            cacheLoaded = true;
        }
    }

    private void loadFromDisk() {
        cacheById.clear();
        cacheByUsername.clear();
        if (!storeFile.exists()) return;
        try {
            JsonNode node = mapper.readTree(storeFile);
            if (!node.isArray()) return;
            for (JsonNode n : (ArrayNode) node) {
                T entity = mapNode(n);
                cacheById.put(entity.getId(), entity);
                cacheByUsername.put(entity.getUsername(), entity);
            }
        } catch (IOException e) {
            System.err.println("Errore loadFromDisk in " + getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected void persistAtomic(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            File tmp = File.createTempFile(storeFile.getName() + "_", ".json", storeFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), storeFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistAtomic in " + getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    protected UUID readId(JsonNode node) {
        return UUID.fromString(node.path("id").asText());
    }

    protected String readString(JsonNode node, String field) {
        return node.path(field).asText();
    }

    protected LocalDate readDate(JsonNode node) {
        return LocalDate.parse(node.path("dateOfBirth").asText());
    }

    protected boolean readBoolean(JsonNode node, String field, boolean defaultValue) {
        return node.path(field).asBoolean(defaultValue);
    }

    protected Location readLocation(JsonNode node) {
        JsonNode locNode = node.path("location");
        if (locNode.isMissingNode() || locNode.isNull()) return null;
        return new Location(
                Nation.fromString(locNode.path("nation").asText()),
                locNode.path("city").asText(),
                locNode.path("latitude").asDouble(),
                locNode.path("longitude").asDouble(),
                locNode.path("address").asText()
        );
    }

    protected void writeUserFields(ObjectNode node, User user) {
        node.put("id", user.getId().toString());
        node.put("username", user.getUsername());
        node.put("passwordHash", user.getPasswordHash());
        node.put("passwordSalt", user.getPasswordSalt());
        node.put("name", user.getName());
        node.put("lastName", user.getLastName());
        node.put("dateOfBirth", user.getDateOfBirth().toString());
        node.put("system", user.isSystem());

        if (user.getLocation() != null) {
            ObjectNode locNode = mapper.createObjectNode();
            locNode.put("nation", user.getLocation().getNation().name());
            locNode.put("city", user.getLocation().getCity());
            locNode.put("latitude", user.getLocation().getLatitude());
            locNode.put("longitude", user.getLocation().getLongitude());
            locNode.put("address", user.getLocation().getAddress());
            node.set("location", locNode);
        }
    }

    @Override
    public Optional<T> findById(UUID id) {
        ensureCacheLoaded();
        T cached = cacheById.get(id);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    public Optional<T> findByUsername(String username) {
        ensureCacheLoaded();
        T cached = cacheByUsername.get(username);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        ensureCacheLoaded();
        return new ArrayList<>(cacheById.values());
    }

    @Override
    public List<T> findAll(int offset, int limit) {
        List<T> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() {
        ensureCacheLoaded();
        return cacheById.size();
    }

    @Override
    public boolean save(T user) {
        if (user == null) return false;
        ensureCacheLoaded();
        if (cacheById.containsKey(user.getId())) return false;
        cacheById.put(user.getId(), user);
        cacheByUsername.put(user.getUsername(), user);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean update(T user) {
        if (user == null) return false;
        ensureCacheLoaded();
        T old = cacheById.get(user.getId());
        if (old == null) return false;
        cacheByUsername.remove(old.getUsername());
        cacheById.put(user.getId(), user);
        cacheByUsername.put(user.getUsername(), user);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        ensureCacheLoaded();
        T removed = cacheById.remove(id);
        if (removed == null) return false;
        cacheByUsername.remove(removed.getUsername());
        persistAtomic(toArrayNode());
        return true;
    }
}
