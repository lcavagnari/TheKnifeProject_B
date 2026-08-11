package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.objects.Customer;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.objects.Owner;
import it.uninsubria.laboratoriob.objects.Restaurant;
import it.uninsubria.laboratoriob.utils.Database;
import it.uninsubria.laboratoriob.utils.Loader;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * it.uninsubria.laboratoriob.data.DAO per l'entità {@link Customer}.
 * <p>
 */
public final class CustomerDAO extends UserDAO<Customer> {

    public CustomerDAO() { super(false); }

    @Override
    protected Customer mapRow(ResultSet rs) throws SQLException {
        UUID uId = UUID.fromString(rs.getString("id"));
        Set<UUID> favourites = findFavourites(uId);

        Optional<Location> loc = locationDAO.findByCoordinates(
                rs.getDouble("latitude"),
                rs.getDouble("longitude")
        );

        return new Customer(
                uId,                                                         //  id
                rs.getString("username"),                       //  username
                rs.getString("psw_hash"),                       //  password_hash
                rs.getString("psw_salt"),                       //  salt
                rs.getString("first_name"),                     //  name
                rs.getString("last_name"),                      //  last_name
                loc.orElse(null),                                     //  location
                LocalDate.parse(rs.getString("birth_date")),    //  date_of_birth
                favourites
        );
    }


    // TODO: aggiungere sistema intelligente per diff.
    @Override
    public boolean update(Customer user) {
        boolean succeded = super.update(user);
        if (succeded) {
            String query = "DELETE FROM user_favorites WHERE user_id=?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query))  {
                stmt.setString(1, user.getId().toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Errore update in CustomerDAO: " + e.getMessage());
                return false;
            }

            for (UUID restId : user.getFavouriteRestourants())
                addSpecial(user.getId(), restId);
        }

        return succeded;
    }





    @Override
    public boolean save(Customer user) {
        boolean succeded = super.save(user);
        if (succeded) {
            for (UUID id : user.getFavouriteRestourants()) {
                Optional<Restaurant> r1 = restaurantDAO.findById(id);
                if (r1.isEmpty()) {
                    Restaurant r = Loader.getRestaurantsById().get(id);
                    restaurantDAO.save(r);
                }

                addSpecial(user.getId(),id);
            }
        }


        return succeded;
    }

    public boolean addFavourites(UUID ownerId, UUID restaurantId) { return super.addSpecial(ownerId, restaurantId); }

    public boolean removeFavourites(UUID ownerId, UUID restaurantId) { return super.removeSpecial(ownerId, restaurantId); }
    public Set<UUID> findFavourites(UUID ownerId) { return super.findSpecial(ownerId); }
}
