package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Client extends User {

    private final Set<Restaurant> favourites;
    private final ArrayNode favouritesArray;

    public Client(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);

        this.favouritesArray = mapper.createArrayNode();
        this.favourites = new HashSet<>();
    }


    public boolean addFavourite(Restaurant r) {
        return r != null && favourites.add(r);
    }

    public boolean removeFavourite(Restaurant r) {
        return r != null && favourites.remove(r);
    }


    @Override
    public void build() {
        super.build();

        jsonObject.put("role","Client");

        favouritesArray.removeAll();
        favourites.forEach(fav -> favouritesArray.add(fav.toString()));

        jsonObject.put("ddd",1);
        jsonObject.set("favourites", favouritesArray);
    }
}
