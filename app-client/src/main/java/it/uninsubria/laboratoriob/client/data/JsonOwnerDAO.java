package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class JsonOwnerDAO extends JsonUserDAO<Owner> {

    private volatile RestaurantServiceInter restaurantService;
    private final JsonRestaurantDAO restaurantDAO;
    private File ownedRestaurantsFile;

    JsonOwnerDAO(AuthServiceInter authService, RestaurantServiceInter restaurantService, JsonRestaurantDAO restaurantDAO) {
        super(Owner.class, UserRole.OWNER, authService);
        this.restaurantService = restaurantService;
        this.restaurantDAO = restaurantDAO;
    }

    @Override
    public void repointTo(UUID userId) {
        super.repointTo(userId);
        File userDir = new File(Constants.ROOT, userId.toString());
        this.ownedRestaurantsFile = new File(userDir, "restaurants.json");
    }

    private Set<UUID> loadOwnedRestaurantIds() {
        if (ownedRestaurantsFile == null || !ownedRestaurantsFile.exists()) return Set.of();
        try {
            JsonNode node = mapper.readTree(ownedRestaurantsFile);
            if (!node.isArray()) return Set.of();
            Set<UUID> ids = new HashSet<>();
            for (JsonNode n : node) ids.add(UUID.fromString(n.asText()));
            return ids;
        } catch (IOException e) {
            System.err.println("Errore loadOwnedRestaurantIds in JsonOwnerDAO: " + e.getMessage());
            return Set.of();
        }
    }

    private void persistOwnedRestaurantIds(Set<UUID> restaurantIds) {
        if (ownedRestaurantsFile == null) return;
        try {
            if (!ownedRestaurantsFile.getParentFile().exists()) ownedRestaurantsFile.getParentFile().mkdirs();
            ArrayNode array = mapper.createArrayNode();
            for (UUID id : restaurantIds) array.add(id.toString());
            File tmp = File.createTempFile("owned_restaurants_", ".json", ownedRestaurantsFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), ownedRestaurantsFile.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistOwnedRestaurantIds in JsonOwnerDAO: " + e.getMessage());
        }
    }

    void setRemoteRestaurantService(RestaurantServiceInter restaurantService) {
        this.restaurantService = restaurantService;
    }

    private RestaurantServiceInter ensureRestaurantService() {
        RestaurantServiceInter current = restaurantService;
        if (current != null) return current;
        RestaurantServiceInter fresh = RmiRepository.lookupRestaurantService();
        if (fresh != null) this.restaurantService = fresh;
        return fresh;
    }

    @Override
    protected Owner mapNode(JsonNode node) {
        Owner owner = new Owner(
                readId(node),
                readString(node, "username"),
                readString(node, "passwordHash"),
                readString(node, "passwordSalt"),
                readString(node, "name"),
                readString(node, "lastName"),
                readLocation(node),
                readDate(node),
                readBoolean(node, "system", false)
        );

        for (UUID restaurantId : loadOwnedRestaurantIds())
            restaurantDAO.findById(restaurantId).ifPresent(owner::addRestaurant);

        return owner;
    }

    @Override
    protected ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Owner owner : cacheById.values()) {
            ObjectNode node = mapper.createObjectNode();
            writeUserFields(node, owner);
            array.add(node);
        }
        return array;
    }

    public boolean addOwnedRestaurant(UUID ownerId, Restaurant restaurant) {
        Owner owner = cacheById.get(ownerId);
        if (owner == null || restaurant == null) return false;

        RestaurantServiceInter svc = ensureRestaurantService();
        if (svc == null) return false;

        try {
            svc.registerOwner(restaurant, ownerId);
        } catch (RemoteException e) {
            this.restaurantService = null;
            throw new ServiceUnavailableException("Server non disponibile", e);
        }

        boolean added = owner.addRestaurant(restaurant);
        if (added) persistOwnedRestaurantIds(owner.getRestaurantsById().keySet());
        return added;
    }

    public boolean removeOwnedRestaurant(UUID ownerId, UUID restaurantId) {
        Owner owner = cacheById.get(ownerId);
        if (owner == null) return false;

        Restaurant restaurant = owner.getRestaurantsById().get(restaurantId);
        if (restaurant == null) return false;

        RestaurantServiceInter svc = ensureRestaurantService();
        if (svc == null) return false;

        try {
            svc.unregisterOwner(ownerId, restaurantId);
        } catch (RemoteException e) {
            this.restaurantService = null;
            throw new ServiceUnavailableException("Server non disponibile", e);
        }

        boolean removed = owner.removeRestaurant(restaurant);
        if (removed) persistOwnedRestaurantIds(owner.getRestaurantsById().keySet());
        return removed;
    }

    public Set<UUID> findOwnedRestaurants(UUID ownerId) {
        Owner owner = cacheById.get(ownerId);
        return owner != null ? Set.copyOf(owner.getRestaurantsById().keySet()) : Set.of();
    }
}
