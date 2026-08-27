package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class JsonCustomerDAO extends JsonUserDAO<Customer> {

    JsonCustomerDAO(AuthServiceInter authService) {
        super(Customer.class, authService);
    }

    @Override
    protected Customer mapNode(JsonNode node) {
        Set<UUID> favourites = new HashSet<>();
        JsonNode favNode = node.path("favouriteRestourants");
        if (favNode.isArray()) {
            for (JsonNode fav : favNode) {
                favourites.add(UUID.fromString(fav.asText()));
            }
        }

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

            ArrayNode favArray = mapper.createArrayNode();
            ((Customer) c).getFavouriteRestourants().forEach(fav -> favArray.add(fav.toString()));
            node.set("favouriteRestourants", favArray);

            array.add(node);
        }
        return array;
    }

    public boolean addFavourite(UUID customerId, UUID restaurantId) {
        Customer customer = (Customer) cacheById.get(customerId);
        if (customer == null) return false;
        customer.getFavouriteRestourants().add(restaurantId);
        persistAtomic(toArrayNode());
        return true;
    }

    public boolean removeFavourite(UUID customerId, UUID restaurantId) {
        Customer customer = (Customer) cacheById.get(customerId);
        if (customer == null) return false;
        customer.getFavouriteRestourants().remove(restaurantId);
        persistAtomic(toArrayNode());
        return true;
    }

    public Set<UUID> findFavourites(UUID customerId) {
        Customer customer = (Customer) cacheById.get(customerId);
        return customer != null ? Set.copyOf(customer.getFavouriteRestourants()) : Set.of();
    }
}
