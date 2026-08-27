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
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonOwnerDAO implements DAO<Owner> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;
    private final AuthServiceInter service;

    private final ConcurrentHashMap<UUID, Owner> cacheById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Owner> cacheByUsername = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonOwnerDAO(AuthServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "owners.json");
        this.service = service;
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
        cacheById.clear();
        cacheByUsername.clear();
        if (!storeFile.exists()) return;
        try {
            JsonNode node = mapper.readTree(storeFile);
            if (!node.isArray()) return;
            for (JsonNode n : (ArrayNode) node) {
                Owner owner = mapNode(n);
                cacheById.put(owner.getId(), owner);
                cacheByUsername.put(owner.getUsername(), owner);
            }
        } catch (IOException e) {
            System.err.println("Errore loadFromDisk in JsonOwnerDAO: " + e.getMessage());
        }
    }

    private void persistAtomic(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            File tmp = File.createTempFile("owners_", ".json", storeFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), storeFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistAtomic in JsonOwnerDAO: " + e.getMessage());
        }
    }

    private ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Owner owner : cacheById.values()) {
            array.add(toNode(owner));
        }
        return array;
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

        return node;
    }

    @Override
    public Optional<Owner> findById(UUID id) {
        ensureCacheLoaded();
        Owner cached = cacheById.get(id);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    public Optional<Owner> findByUsername(String username) {
        ensureCacheLoaded();
        Owner cached = cacheByUsername.get(username);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    @Override
    public List<Owner> findAll() {
        ensureCacheLoaded();
        return new ArrayList<>(cacheById.values());
    }

    @Override
    public List<Owner> findAll(int offset, int limit) {
        List<Owner> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() {
        ensureCacheLoaded();
        return cacheById.size();
    }

    @Override
    public boolean save(Owner owner) {
        if (owner == null) return false;
        ensureCacheLoaded();
        if (cacheById.containsKey(owner.getId())) return false;
        cacheById.put(owner.getId(), owner);
        cacheByUsername.put(owner.getUsername(), owner);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean update(Owner owner) {
        if (owner == null) return false;
        ensureCacheLoaded();
        Owner old = cacheById.get(owner.getId());
        if (old == null) return false;
        cacheByUsername.remove(old.getUsername());
        cacheById.put(owner.getId(), owner);
        cacheByUsername.put(owner.getUsername(), owner);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        ensureCacheLoaded();
        Owner removed = cacheById.remove(id);
        if (removed == null) return false;
        cacheByUsername.remove(removed.getUsername());
        persistAtomic(toArrayNode());
        return true;
    }
}
