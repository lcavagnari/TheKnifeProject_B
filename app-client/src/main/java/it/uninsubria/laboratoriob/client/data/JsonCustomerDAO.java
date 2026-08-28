package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class JsonCustomerDAO extends JsonUserDAO<Customer> {

    private volatile FavouriteServiceInter favService;
    private File favouritesFile;

    JsonCustomerDAO(AuthServiceInter authService, FavouriteServiceInter favService) {
        super(Customer.class, UserRole.CLIENT, authService);

        this.favService = favService;
        this.favouritesFile = new File(Constants.ROOT, "favourites.json");
    }

    @Override
    public void repointTo(UUID userId) {
        super.repointTo(userId);
        File userDir = new File(Constants.ROOT, userId.toString());
        this.favouritesFile = new File(userDir, favouritesFile.getName());
    }

    private Set<UUID> loadFavouritesFromDisk() {
        if (!favouritesFile.exists()) return new HashSet<>();
        try {
            JsonNode node = mapper.readTree(favouritesFile);
            if (!node.isArray()) return new HashSet<>();
            Set<UUID> ids = new HashSet<>();
            for (JsonNode n : node) ids.add(UUID.fromString(n.asText()));
            return ids;
        } catch (IOException e) {
            System.err.println("Errore loadFavouritesFromDisk in JsonCustomerDAO: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private void persistFavourites(Set<UUID> favouriteIds) {
        try {
            if (!favouritesFile.getParentFile().exists()) favouritesFile.getParentFile().mkdirs();
            ArrayNode array = mapper.createArrayNode();
            for (UUID id : favouriteIds) array.add(id.toString());
            File tmp = File.createTempFile("favourites_", ".json", favouritesFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), favouritesFile.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistFavourites in JsonCustomerDAO: " + e.getMessage());
        }
    }

    void setRemoteFavService(FavouriteServiceInter favService) {
        this.favService = favService;
    }

    private FavouriteServiceInter ensureFavService() {
        FavouriteServiceInter current = favService;
        if (current != null) return current;
        FavouriteServiceInter fresh = RmiRepository.lookupFavouriteService();
        if (fresh != null) this.favService = fresh;
        return fresh;
    }

    @Override
    protected Customer mapNode(JsonNode node) {
        Set<UUID> favourites = loadFavouritesFromDisk();

        return new Customer(
                readId(node),
                readString(node, "username"),
                readString(node, "passwordHash"),
                readString(node, "passwordSalt"),
                readString(node, "name"),
                readString(node, "lastName"),
                readLocation(node),
                readDate(node),
                favourites,
                readBoolean(node, "system", false)
        );
    }

    @Override
    protected ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();

        for (Customer c : cacheById.values()) {
            ObjectNode node = mapper.createObjectNode();
            writeUserFields(node, c);
            array.add(node);
        }

        return array;
    }

    public boolean addFavourite(UUID customerId, UUID restaurantId) {
        Customer customer = cacheById.get(customerId);
        if (customer == null) return false;

        FavouriteServiceInter svc = ensureFavService();
        if (svc != null) {
            try {
                svc.addFavourites(customerId, restaurantId);
            } catch (RemoteException e) {
                this.favService = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }

        customer.getFavouriteRestourants().add(restaurantId);
        persistFavourites(customer.getFavouriteRestourants());
        return true;
    }

    public boolean removeFavourite(UUID customerId, UUID restaurantId) {
        Customer customer = cacheById.get(customerId);
        if (customer == null) return false;

        FavouriteServiceInter svc = ensureFavService();
        if (svc != null) {
            try {
                svc.removeFavourites(customerId, restaurantId);
            } catch (RemoteException e) {
                this.favService = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }

        customer.getFavouriteRestourants().remove(restaurantId);
        persistFavourites(customer.getFavouriteRestourants());
        return true;
    }

    public Set<UUID> findFavourites(UUID customerId) {
        Customer customer = cacheById.get(customerId);
        return customer != null ? Set.copyOf(customer.getFavouriteRestourants()) : Set.of();
    }
}
