package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Client extends User {

    private final Set<UUID> favouriteRestourants;
    private final ArrayNode favouritesArray;

    public Client(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);

        this.favouritesArray = mapper.createArrayNode();
        this.favouriteRestourants = new HashSet<>();

        build();
    }

    public Client(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, name, lastName, location, dateOfBirth, password, salt);

        this.favouritesArray = mapper.createArrayNode();
        this.favouriteRestourants = new HashSet<>();

        build();
    }

    public Client(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth, Set<UUID> favouriteRestourants) {
        super(username, name, lastName, location, dateOfBirth, password, salt);
        this.favouriteRestourants = favouriteRestourants;
        this.favouritesArray = mapper.createArrayNode();
    }

    public boolean addFavourite(Restaurant r) {
        return r != null && favouriteRestourants.add(r.getId());
    }

    public boolean removeFavourite(Restaurant r) {
        return r != null && favouriteRestourants.remove(r.getId());
    }


    @Override
    public void build() {
        super.build();

        jsonObject.put("role", "Client");

        if (favouritesArray != null) {
            favouritesArray.removeAll();
            favouriteRestourants.forEach(fav -> favouritesArray.add(fav.toString()));
        }

        jsonObject.set("favourites", favouritesArray);
    }

    @Override
    public String toString() {
        return "Client{" +
                super.toString() +
                ", favourites=" + favouriteRestourants +
                '}';
    }
}
