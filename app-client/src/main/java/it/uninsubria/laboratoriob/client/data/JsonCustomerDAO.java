package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonCustomerDAO implements DAO<Customer> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;
    private final AuthServiceInter service;

    private final ConcurrentHashMap<UUID, Customer> cacheById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Customer> cacheByUsername = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonCustomerDAO(AuthServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "customers.json");
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
                Customer customer = mapNode(n);
                cacheById.put(customer.getId(), customer);
                cacheByUsername.put(customer.getUsername(), customer);
            }
        } catch (IOException e) {
            System.err.println("Errore loadFromDisk in JsonCustomerDAO: " + e.getMessage());
        }
    }

    private void persistAtomic(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            File tmp = File.createTempFile("customers_", ".json", storeFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), storeFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistAtomic in JsonCustomerDAO: " + e.getMessage());
        }
    }

    private ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Customer customer : cacheById.values()) {
            array.add(toNode(customer));
        }
        return array;
    }

    private Customer mapNode(JsonNode node) {
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

        Set<UUID> favourites = new HashSet<>();
        JsonNode favNode = node.path("favouriteRestourants");
        if (favNode.isArray()) {
            for (JsonNode fav : favNode) {
                favourites.add(UUID.fromString(fav.asText()));
            }
        }

        return new Customer(id, username, passwordHash, passwordSalt, name, lastName, loc, dateOfBirth, favourites, system);
    }

    private ObjectNode toNode(Customer customer) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", customer.getId().toString());
        node.put("username", customer.getUsername());
        node.put("passwordHash", customer.getPasswordHash());
        node.put("passwordSalt", customer.getPasswordSalt());
        node.put("name", customer.getName());
        node.put("lastName", customer.getLastName());
        node.put("dateOfBirth", customer.getDateOfBirth().toString());
        node.put("system", customer.isSystem());

        if (customer.getLocation() != null) {
            ObjectNode locNode = mapper.createObjectNode();
            locNode.put("nation", customer.getLocation().getNation().name());
            locNode.put("city", customer.getLocation().getCity());
            locNode.put("latitude", customer.getLocation().getLatitude());
            locNode.put("longitude", customer.getLocation().getLongitude());
            locNode.put("address", customer.getLocation().getAddress());
            node.set("location", locNode);
        }

        ArrayNode favArray = mapper.createArrayNode();
        customer.getFavouriteRestourants().forEach(fav -> favArray.add(fav.toString()));
        node.set("favouriteRestourants", favArray);

        return node;
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        ensureCacheLoaded();
        Customer cached = cacheById.get(id);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    public Optional<Customer> findByUsername(String username) {
        ensureCacheLoaded();
        Customer cached = cacheByUsername.get(username);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        ensureCacheLoaded();
        return new ArrayList<>(cacheById.values());
    }

    @Override
    public List<Customer> findAll(int offset, int limit) {
        List<Customer> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() {
        ensureCacheLoaded();
        return cacheById.size();
    }

    @Override
    public boolean save(Customer customer) {
        if (customer == null) return false;
        ensureCacheLoaded();
        if (cacheById.containsKey(customer.getId())) return false;
        cacheById.put(customer.getId(), customer);
        cacheByUsername.put(customer.getUsername(), customer);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean update(Customer customer) {
        if (customer == null) return false;
        ensureCacheLoaded();
        Customer old = cacheById.get(customer.getId());
        if (old == null) return false;
        cacheByUsername.remove(old.getUsername());
        cacheById.put(customer.getId(), customer);
        cacheByUsername.put(customer.getUsername(), customer);
        persistAtomic(toArrayNode());
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        ensureCacheLoaded();
        Customer removed = cacheById.remove(id);
        if (removed == null) return false;
        cacheByUsername.remove(removed.getUsername());
        persistAtomic(toArrayNode());
        return true;
    }

    public boolean addFavourite(UUID customerId, UUID restaurantId) {
        ensureCacheLoaded();
        Customer customer = cacheById.get(customerId);
        if (customer == null) return false;
        customer.getFavouriteRestourants().add(restaurantId);
        persistAtomic(toArrayNode());
        return true;
    }

    public boolean removeFavourite(UUID customerId, UUID restaurantId) {
        ensureCacheLoaded();
        Customer customer = cacheById.get(customerId);
        if (customer == null) return false;
        customer.getFavouriteRestourants().remove(restaurantId);
        persistAtomic(toArrayNode());
        return true;
    }

    public Set<UUID> findFavourites(UUID customerId) {
        ensureCacheLoaded();
        Customer customer = cacheById.get(customerId);
        return customer != null ? Set.copyOf(customer.getFavouriteRestourants()) : Set.of();
    }
}
