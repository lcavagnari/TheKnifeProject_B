package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Classe che rappresenta un utente di tipo Proprietario (Owner).<p>
 * Mantiene un insieme di ristoranti di cui è proprietario.<p>
 * Fornisce metodi per aggiungere, rimuovere e modificare ristoranti.<p>
 * Estende la classe {@link User} aggiungendo funzionalità specifiche del proprietario.<p>
 * <p>
 * Autore: Luke
 *
 * @version 1.1
 */
@Getter
public class Owner extends User {

    /**
     * Insieme dei ristoranti posseduti dal proprietario.
     */

    /**
     * Mappa dei ristoranti indicizzati per ID.
     */
    @Getter
    private final Map<UUID, Restaurant> restaurantsById = new HashMap<>();

    /**
     * Mappa dei ristoranti indicizzati per nome.
     */
    @Getter
    private final Map<String, Restaurant> restaurantsByName = new HashMap<>();

    public Owner(UUID id, String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth, Set<Restaurant> restaurants) {
        super(id, username, name, lastName, name, lastName, location, dateOfBirth);

        if (restaurants == null || restaurants.isEmpty()) return;
        for (Restaurant r : restaurants) {
            restaurantsById.put(r.getId(), r);
            restaurantsByName.put(r.getName(), r);
        }
    }

    public Owner(UUID id, String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(id, username, password, salt, name, lastName, location, dateOfBirth);
    }

    public Owner(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);
    }

    public boolean addRestaurant(Restaurant r) {
        if (r == null || restaurantsById.containsKey(r.getId())) return false;
        restaurantsById.put(r.getId(), r);
        restaurantsByName.put(r.getName(), r);
        return true;
    }

    public boolean removeRestaurant(Restaurant r) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;
        restaurantsById.remove(r.getId());
        restaurantsByName.remove(r.getName());
        return true;
    }

    public boolean renameRestaurant(UUID id, String newName) {
        if (id == null || newName == null || newName.isBlank() || !newName.matches("[\\p{L}0-9 \\-']{4,30}$") || !restaurantsById.containsKey(id))
            return false;

        Restaurant r = restaurantsById.get(id);
        if (r == null) {
            restaurantsById.remove(id);
            return false;
        }

        r.setName(newName);
        r.build();

        return true;
    }


    public boolean modifyRestaurantDescription(Restaurant r, String newDescription) {
        if (r == null || newDescription == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setDescription(newDescription);
        r.build();

        return true;
    }

    public boolean modifyRestaurantWebsite(Restaurant r, String newWebsiteUrl) {
        if (r == null || newWebsiteUrl == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setWebsiteUrl(newWebsiteUrl);
        r.build();

        return true;
    }

    public boolean modifyRestaurantPhone(Restaurant r, String newPhone) {
        if (r == null || newPhone == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setPhone(newPhone);
        build();

        return true;
    }

    public boolean modifyRestaurantLocation(Restaurant r, Location newLocation) {
        if (r == null || newLocation == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setLocation(newLocation);
        build();

        return true;
    }

    public boolean modifyRestaurantPriceRange(Restaurant r, PriceRange newPriceRange) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setPriceRange(newPriceRange);
        build();

        return true;
    }

    public boolean modifyRestaurantAward(Restaurant r, Award newAward) {
        if (r == null || newAward == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setAward(newAward);
        build();

        return true;
    }

    public boolean modifyRestaurantGreenStar(Restaurant r, boolean greenStar) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setGreenStar(greenStar);
        build();

        return true;
    }

    public boolean modifyRestaurantHasDelivery(Restaurant r, boolean hasDelivery) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setHasDelivery(hasDelivery);
        build();
        return true;
    }

    public boolean modifyRestaurantHasBooking(Restaurant r, boolean hasBooking) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setHasOnlineBooking(hasBooking);
        build();

        return true;
    }


    public String showRestaurantDetails(Restaurant r) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return null;
        return r.toString();
    }


    @Override
    public UserRole getRole() {
        return UserRole.OWNER;
    }
}
