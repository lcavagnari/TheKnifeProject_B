package it.uninsubria.laboratoriob.api.data;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class OwnerDAO extends UserDAO<Owner> {

    public OwnerDAO() { super(true); }

    @Override
    protected Owner mapRow(ResultSet rs) throws SQLException {
        UUID uId = UUID.fromString(rs.getString("id"));
        Set<Restaurant> restaurants = new HashSet<>(restaurantDAO.findByOwner(uId));

        Optional<Location> loc = locationDAO.findByCoordinates(
                rs.getDouble("latitude"),
                rs.getDouble("longitude")
        );

        return new Owner(
                uId,                                                         //  id
                rs.getString("username"),                       //  username
                rs.getString("psw_hash"),                       //  password_hash
                rs.getString("psw_salt"),                       //  salt
                rs.getString("first_name"),                     //  name
                rs.getString("last_name"),                      //  last_name
                loc.orElse(null),                                     //  location
                LocalDate.parse(rs.getString("birth_date")),    //  date_of_birth
                restaurants
        );
    }

    @Override
    public boolean save(Owner user) {
        boolean succeded = super.save(user);
        if (succeded) {
            for (Restaurant r : user.getRestaurantsById().values()) {
                Optional<Restaurant> r1 = restaurantDAO.findById(r.getId());
                if (r1.isEmpty()) restaurantDAO.save(r);

                addSpecial(r.getId(),user.getId());
            }
        }


        return succeded;
    }

    public boolean addRestaurant(UUID ownerId, UUID restaurantId) { return super.addSpecial(ownerId, restaurantId); }
    public boolean removeRestaurant(UUID ownerId, UUID restaurantId) { return super.removeSpecial(ownerId, restaurantId); }
    public Set<UUID> findRestaurants(UUID ownerId) { return super.findSpecial(ownerId); }
}
