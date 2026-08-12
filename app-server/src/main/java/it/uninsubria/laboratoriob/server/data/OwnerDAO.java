package it.uninsubria.laboratoriob.server.data;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.server.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementazione del DAO per l'entità {@link Owner}.
 * <p>
 * Estende {@link UserDAO} specializzando le operazioni per i proprietari di ristoranti.
 * Gestisce la associazione many-to-many tra owner e ristoranti tramite la tabella
 * {@code user_restaurants}.
 * </p>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *   <li>Mappatura delle righe del ResultSet in oggetti {@link Owner} con i ristoranti associati.</li>
 *   <li>Gestione del salvataggio e aggiornamento dell'owner con i suoi ristoranti.</li>
 *   <li>Operazioni di aggiunta, rimozione e ricerca ristoranti per owner.</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see UserDAO
 * @see Owner
 */
public class OwnerDAO extends UserDAO<Owner> {

    public OwnerDAO() {
        super(true);
    }

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
                restaurants,                                               //  restaurants
                rs.getBoolean("is_system")                      //  system
        );
    }

    @Override
    public boolean save(Owner user) {
        boolean succeded = super.save(user);
         if (succeded) {
            for (Restaurant r : user.getRestaurantsById().values()) {
                Optional<Restaurant> r1 = restaurantDAO.findById(r.getId());
                if (r1.isEmpty()) restaurantDAO.save(r);

                addSpecial(user.getId(), r.getId());
            }
        }


        return succeded;
    }

    @Override
    public boolean update(Owner user) {
        boolean succeded = super.update(user);
        if (succeded) {
            String query = "DELETE FROM user_restaurants WHERE user_id=?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, user.getId(), java.sql.Types.OTHER);
                stmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Errore update in OwnerDAO: " + e.getMessage());
                return false;
            }

            for (Restaurant r : user.getRestaurantsById().values())
                addSpecial(user.getId(), r.getId());
        }

        return succeded;
    }

    public boolean addRestaurant(UUID ownerId, UUID restaurantId) {
        return super.addSpecial(ownerId, restaurantId);
    }

    public boolean removeRestaurant(UUID ownerId, UUID restaurantId) {
        return super.removeSpecial(ownerId, restaurantId);
    }

    public Set<UUID> findRestaurants(UUID ownerId) {
        return super.findSpecial(ownerId);
    }
}
