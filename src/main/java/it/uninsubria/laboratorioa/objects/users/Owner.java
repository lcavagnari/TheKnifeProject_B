package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Owner extends User {

    private final Set<Restaurant> restaurants;

    public Owner(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, name, lastName, location, dateOfBirth, password, salt);

        this.restaurants = new HashSet<>();
    }

    public Owner(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);

        this.restaurants = new HashSet<>();
    }

    public Owner(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth, Set<Restaurant> restaurants) {
        super(username, name, lastName, location, dateOfBirth, password, salt);

        this.restaurants = (restaurants == null) ? new HashSet<>() : restaurants;
    }

    public boolean addRestaurant(Restaurant r) {
        return r != null && restaurants.add(r);
    }

    public boolean removeRestaurant(Restaurant r) {
        return r != null && restaurants.remove(r);
    }

    @Override
    protected void build() {
        super.build();

        jsonObject.put("role", "Owner");
    }
}
