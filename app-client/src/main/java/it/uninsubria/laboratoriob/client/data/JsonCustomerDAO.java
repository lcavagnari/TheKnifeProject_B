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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Implementazione JSON del DAO per l'entità {@link Customer}.
 * <p>
 * Gestisce le operazioni CRUD sui clienti usando file JSON come storage locale.
 * I dati vengono memorizzati in un singolo file JSON array.
 * </p>
 */
public final class JsonCustomerDAO implements DAO<Customer> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;

    public JsonCustomerDAO() {
        this.storeFile = new File(Constants.ROOT, "customers.json");
    }

    private ArrayNode loadAll() {
        if (!storeFile.exists()) return mapper.createArrayNode();
        try {
            JsonNode node = mapper.readTree(storeFile);
            return node.isArray() ? (ArrayNode) node : mapper.createArrayNode();
        } catch (IOException e) {
            System.err.println("Errore loadAll in JsonCustomerDAO: " + e.getMessage());
            return mapper.createArrayNode();
        }
    }

    private void persist(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(storeFile, array);
        } catch (IOException e) {
            System.err.println("Errore persist in JsonCustomerDAO: " + e.getMessage());
        }
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
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (node.path("id").asText().equals(id.toString())) {
                return Optional.of(mapNode(node));
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByUsername(String username) {
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (node.path("username").asText().equals(username)) {
                return Optional.of(mapNode(node));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        ArrayNode array = loadAll();
        List<Customer> customers = new ArrayList<>();
        for (JsonNode node : array) {
            customers.add(mapNode(node));
        }
        return customers;
    }

    @Override
    public boolean save(Customer customer) {
        if (customer == null) return false;
        ArrayNode array = loadAll();

        for (JsonNode node : array) {
            if (node.path("id").asText().equals(customer.getId().toString())) {
                return false;
            }
        }

        array.add(toNode(customer));
        persist(array);
        return true;
    }

    @Override
    public boolean update(Customer customer) {
        if (customer == null) return false;
        ArrayNode array = loadAll();

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).path("id").asText().equals(customer.getId().toString())) {
                array.set(i, toNode(customer));
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

    public boolean addFavourite(UUID customerId, UUID restaurantId) {
        Optional<Customer> opt = findById(customerId);
        if (opt.isEmpty()) return false;
        Customer customer = opt.get();
        customer.getFavouriteRestourants().add(restaurantId);
        return update(customer);
    }

    public boolean removeFavourite(UUID customerId, UUID restaurantId) {
        Optional<Customer> opt = findById(customerId);
        if (opt.isEmpty()) return false;
        Customer customer = opt.get();
        customer.getFavouriteRestourants().remove(restaurantId);
        return update(customer);
    }

    public Set<UUID> findFavourites(UUID customerId) {
        Optional<Customer> opt = findById(customerId);
        return opt.map(Customer::getFavouriteRestourants).orElse(Set.of());
    }
}
