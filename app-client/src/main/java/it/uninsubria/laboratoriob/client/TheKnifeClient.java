package it.uninsubria.laboratoriob.client;

import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.ui.menus.GuestMenus;
import it.uninsubria.laboratoriob.client.utils.HeartbeatClient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Classe principale del client The Knife.
 * <p>
 * Inizializza il {@link ClientDataStore} e fornisce un punto di accesso
 * ai dati memorizzati localmente in cache JSON.
 * </p>
 */
public class TheKnifeClient {

    public static void main(String[] args) {
        IO.printSuccessMessage("Loading The Knife Client...");

        TheKnifeClient client = new TheKnifeClient();

        IO.printSuccessMessage("Client initialized. Data store ready.");

        new GuestMenus(client.dataStore).openMenu();
    }

    private static final String serverHost = "localhost";
    private final int rmiPort = 1099;
    private static final int heartbeatPort = 5555;
    private static final long heartbeatIntervalMinutes = 5;

    private final HeartbeatClient tcpHbeatClient;
    private final ClientDataStore dataStore;

    public TheKnifeClient() {
        this.tcpHbeatClient = new HeartbeatClient(serverHost, heartbeatPort, heartbeatIntervalMinutes);

        RestaurantServiceInter restaurantService = null;
        AuthServiceInter authService = null;
        ReviewServiceInter reviewService = null;
        FavouriteServiceInter favouriteService = null;

        try {
            Registry registry = LocateRegistry.getRegistry(serverHost, rmiPort);
            restaurantService = (RestaurantServiceInter) registry.lookup("restaurant");
            authService = (AuthServiceInter) registry.lookup("auth");
            reviewService = (ReviewServiceInter) registry.lookup("review");
            favouriteService = (FavouriteServiceInter) registry.lookup("favourite");
            IO.printSuccessMessage("RMI connection established.");
        } catch (Exception e) {
            System.err.println("WARNING: RMI lookup failed: " + e.getMessage());
        }

        this.dataStore = new ClientDataStore(restaurantService, authService, reviewService, favouriteService);

        tcpHbeatClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(tcpHbeatClient::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(IO::closeScanner));
    }
}
