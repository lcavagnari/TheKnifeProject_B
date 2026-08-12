package it.uninsubria.laboratoriob.client;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;

import java.util.List;
import java.util.Optional;

/**
 * Classe principale del client The Knife.
 * <p>
 * Inizializza il {@link ClientDataStore} e fornisce un punto di accesso
 * ai dati memorizzati localmente in cache JSON.
 * </p>
 * <p>
 * In futuro, qui verrà integrato il layer di comunicazione RMI
 * per sincronizzare i dati con il server.
 * </p>
 */
public class TheKnifeClient {

    private final ClientDataStore dataStore;

    public TheKnifeClient() {
        this.dataStore = new ClientDataStore();
    }

    public ClientDataStore getDataStore() {
        return dataStore;
    }

    public Optional<Customer> loginCustomer(String username, String password) {
        return dataStore.customerDAO().findByUsername(username)
                .filter(c -> c.getPasswordHash().equals(password));
    }

    public Optional<Owner> loginOwner(String username, String password) {
        return dataStore.ownerDAO().findByUsername(username)
                .filter(o -> o.getPasswordHash().equals(password));
    }

    public List<Restaurant> searchRestaurantsByName(String name) {
        return dataStore.restaurantDAO().findAll().stream()
                .filter(r -> r.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public List<Restaurant> getRestaurantsByOwner(Owner owner) {
        return dataStore.restaurantDAO().findByOwner(owner.getId());
    }

    public static void main(String[] args) {
        System.out.println("The Knife Client starting...");
        TheKnifeClient client = new TheKnifeClient();
        System.out.println("Client initialized. Data store ready.");
    }
}
