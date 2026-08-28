package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

public final class JsonOwnerDAO extends JsonUserDAO<Owner> {

    private final RestaurantServiceInter restaurantService;

    JsonOwnerDAO(AuthServiceInter authService, RestaurantServiceInter restaurantService) {
        super(Owner.class, UserRole.OWNER, authService);
        this.restaurantService = restaurantService;
    }

    @Override
    protected Owner mapNode(JsonNode node) {
        return new Owner(
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

        try {
            restaurantService.saveForOwner(restaurant, ownerId);
        } catch (RemoteException e) {
            System.err.println("RMI sync addOwnedRestaurant " + getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }

        return owner.addRestaurant(restaurant);
    }

    public boolean removeOwnedRestaurant(UUID ownerId, UUID restaurantId) {
        Owner owner = cacheById.get(ownerId);
        if (owner == null) return false;

        Restaurant restaurant = owner.getRestaurantsById().get(restaurantId);
        if (restaurant == null) return false;

        try {
            restaurantService.deleteOwnedRestaurant(ownerId, restaurantId);
        } catch (RemoteException e) {
            System.err.println("RMI sync removeOwnedRestaurant " + getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }

        return owner.removeRestaurant(restaurant);
    }

    public Set<UUID> findOwnedRestaurants(UUID ownerId) {
        Owner owner = cacheById.get(ownerId);
        return owner != null ? Set.copyOf(owner.getRestaurantsById().keySet()) : Set.of();
    }
}
