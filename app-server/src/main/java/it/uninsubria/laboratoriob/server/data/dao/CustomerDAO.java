package it.uninsubria.laboratoriob.server.data.dao;


import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementazione del DAO per l'entità {@link Customer}.
 * <p>
 * Estende {@link UserDAO} specializzando le operazioni per i clienti.
 * Gestisce l'associazione many-to-many tra clienti e ristoranti preferiti
 * tramite la tabella {@code user_favorites}.
 * </p>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *   <li>Mappatura delle righe del ResultSet in oggetti {@link Customer} con i preferiti associati.</li>
 *   <li>Gestione del salvataggio e aggiornamento del customer con i suoi preferiti.</li>
 *   <li>Operazioni di aggiunta, rimozione e ricerca ristoranti preferiti.</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see UserDAO
 * @see Customer
 */
public final class CustomerDAO extends UserDAO<Customer> {

    public CustomerDAO() {
        super(false);
    }

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
                favourites,                                                //  favourites
                rs.getBoolean("is_system")                      //  system
        );
    }


    @Override
    public boolean update(Customer user) {
        boolean succeded = super.update(user);
        if (succeded) {
            Set<UUID> current = findFavourites(user.getId());
            Set<UUID> target = user.getFavouriteRestourants();

            Set<UUID> toRemove = new HashSet<>(current);
            toRemove.removeAll(target);

            Set<UUID> toAdd = new HashSet<>(target);
            toAdd.removeAll(current);

            for (UUID id : toRemove) removeSpecial(user.getId(), id);
            for (UUID id : toAdd) addSpecial(user.getId(), id);
        }

        return succeded;
    }


    @Override
    public boolean save(Customer user) {
        boolean succeded = super.save(user);
        if (succeded) {
            for (UUID id : user.getFavouriteRestourants()) addSpecial(user.getId(), id);
        }


        return succeded;
    }

    public boolean addFavourites(UUID ownerId, UUID restaurantId) {
        return super.addSpecial(ownerId, restaurantId);
    }

    public boolean removeFavourites(UUID ownerId, UUID restaurantId) {
        return super.removeSpecial(ownerId, restaurantId);
    }

    public Set<UUID> findFavourites(UUID ownerId) {
        return super.findSpecial(ownerId);
    }
}
