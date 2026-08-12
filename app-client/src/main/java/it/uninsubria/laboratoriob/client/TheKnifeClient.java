package it.uninsubria.laboratoriob.client;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.utils.HeartbeatClient;

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

    public static void main(String[] args) {
        System.out.println("The Knife Client starting...");

        TheKnifeClient client = new TheKnifeClient();

        System.out.println("Client initialized. Data store ready.");
    }

    private static final String serverHost = "localhost";
    private final int rmiPort = 1099;
    private static final int heartbeatPort = 5555;
    private static final long heartbeatIntervalMinutes = 5;

    private final HeartbeatClient tcpHbeatClient;
    private final ClientDataStore dataStore;

    public TheKnifeClient() {
        this.dataStore = new ClientDataStore();
        this.tcpHbeatClient = new HeartbeatClient(serverHost, heartbeatPort, heartbeatIntervalMinutes);

        tcpHbeatClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(tcpHbeatClient::shutdown));
    }

    public Optional<Customer> loginCustomer(String username, String password) {
        return dataStore.getCustomerDAO().findByUsername(username)
                .filter(c -> c.getPasswordHash().equals(password));
    }

    public Optional<Owner> loginOwner(String username, String password) {
        return dataStore.getOwnerDAO().findByUsername(username)
                .filter(o -> o.getPasswordHash().equals(password));
    }

    public List<Restaurant> searchRestaurantsByName(String name) {
        return dataStore.getRestaurantDAO().findAll().stream()
                .filter(r -> r.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public List<Restaurant> getRestaurantsByOwner(Owner owner) {
        return dataStore.getRestaurantDAO().findByOwner(owner.getId());
    }

    public void shutdown() {
        tcpHbeatClient.shutdown();
        IO.closeScanner();
    }
}
