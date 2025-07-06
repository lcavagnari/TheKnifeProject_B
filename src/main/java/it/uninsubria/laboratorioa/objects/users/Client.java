package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.Restaurant;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Client extends User {

    private final Set<Restaurant> favourites;
    private final ArrayNode favouritesArray;

    public Client(String username, String password, String name, String lastName, LocalDate dateOfBirth) {
        super(username, password, name, lastName, dateOfBirth);

        this.favouritesArray = mapper.createArrayNode();
        this.favourites = new HashSet<>();
    }


    public void addFavourite(Restaurant r) {
        if (r != null && favourites.add(r)) {
            favouritesArray.add(r.getId().toString());
            rebuild();
        }
    }

    public void removeFavourite(Restaurant r) {
        if (r != null && favourites.remove(r)) {
            favouritesArray.removeAll();

            favourites.forEach(res -> favouritesArray.add(res.getId().toString()));
            rebuild();
        }
    }


    @Override
    protected void build() {
        super.build();

        favouritesArray.removeAll();
        favourites.forEach(fav -> favouritesArray.add(fav.toString()));

        jsonObject.set("favourites", favouritesArray);
    }
}
