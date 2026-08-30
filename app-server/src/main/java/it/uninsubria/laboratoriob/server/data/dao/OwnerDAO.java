package it.uninsubria.laboratoriob.server.data.dao;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Costruttore predefinito.
     * <p>
     * Inizializza il DAO impostando il flag {@code isOwner} a {@code true},
     * indicando che questo DAO gestisce utenti di tipo proprietario.
     * </p>
     */
    public OwnerDAO() {
        super(true);
    }

    @Override
    protected Owner mapRow(ResultSet rs) throws SQLException {
        UUID uId = UUID.fromString(rs.getString("id"));
        Set<Restaurant> restaurants = findSpecial(uId).stream()
                .map(restaurantDAO::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

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
            Set<UUID> current = findSpecial(user.getId());
            Set<UUID> target = user.getRestaurantsById().keySet();

            Set<UUID> toRemove = new HashSet<>(current);
            toRemove.removeAll(target);

            Set<UUID> toAdd = new HashSet<>(target);
            toAdd.removeAll(current);

            for (UUID id : toRemove) removeSpecial(user.getId(), id);
            for (UUID id : toAdd) addSpecial(user.getId(), id);
        }

        return succeded;
    }

    /**
     * Aggiunge un'associazione tra un proprietario e un ristorante.
     *
     * @param ownerId      l'UUID del proprietario
     * @param restaurantId l'UUID del ristorante da associare
     * @return {@code true} se l'associazione è stata creata con successo,
     *         {@code false} in caso di errore o se l'associazione esiste già
     */
    public boolean addRestaurant(UUID ownerId, UUID restaurantId) {
        return super.addSpecial(ownerId, restaurantId);
    }

    /**
     * Rimuove l'associazione tra un proprietario e un ristorante.
     *
     * @param ownerId      l'UUID del proprietario
     * @param restaurantId l'UUID del ristorante da dissociare
     * @return {@code true} se l'associazione è stata rimossa con successo,
     *         {@code false} in caso di errore o se l'associazione non esiste
     */
    public boolean removeRestaurant(UUID ownerId, UUID restaurantId) {
        return super.removeSpecial(ownerId, restaurantId);
    }

    /**
     * Trova tutti gli UUID dei ristoranti associati a un proprietario.
     *
     * @param ownerId l'UUID del proprietario
     * @return un {@link Set} contenente gli UUID dei ristoranti associati,
     *         vuoto se il proprietario non possiede ristoranti
     */
    public Set<UUID> findRestaurants(UUID ownerId) {
        return super.findSpecial(ownerId);
    }
}
