package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.client.ui.IO;
import lombok.Getter;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

/**
 * Facade che orchestra tutti i DAO del client.
 * <p>
 * Fornisce accesso centralizzato a tutti i data access object per le entità del sistema.
 * I dati vengono persistiti localmente su file JSON, fungendo da cache lato client
 * dei dati del server.
 * </p>
 * <p>
 * Gestisce internamente la connessione RMI al server: se il server è raggiungibile,
 * gli stub remoti vengono usati per le operazioni di lettura/scrittura;
 * altrimenti si fallback sui DAO locali JSON.
 * </p>
 */
@Getter
public class ClientDataStore {

    private final JsonCustomerDAO customerDAO;
    private final JsonOwnerDAO ownerDAO;
    private final JsonRestaurantDAO restaurantDAO;
    private final JsonReviewDAO reviewDAO;
    private final JsonLocationDAO locationDAO;

    private final RestaurantServiceInter restaurantService;
    private final AuthServiceInter authService;
    private final ReviewServiceInter reviewService;
    private final FavouriteServiceInter favouriteService;

    public ClientDataStore() {
        this.restaurantService = restaurantService;
        this.authService = authService;
        this.reviewService = reviewService;
        this.favouriteService = favouriteService;
        this.customerDAO = new JsonCustomerDAO(authService,favouriteService);
        this.ownerDAO = new JsonOwnerDAO(authService, restaurantService);
        this.locationDAO = new JsonLocationDAO();
        this.restaurantDAO = new JsonRestaurantDAO(restaurantService, locationDAO);
        this.reviewDAO = new JsonReviewDAO(customerDAO, reviewService);
    }

    public boolean acquireRemoteServices(String hostname, int rmiPort, int maxAttempts) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(hostname, rmiPort);

        } catch (RemoteException ignored) {
            int attempts = 0;

            while (attempts <= maxAttempts) {
                try {
                    Thread.currentThread().wait(500);
                } catch (InterruptedException ignored1) {}

                try {
                    registry = LocateRegistry.getRegistry(hostname, rmiPort);
                } catch (RemoteException e) { attempts++; }
            }

            if (registry == null) return false;
        }


        // TODO: add retrieving of services via completable futures.

        try {


            restaurantService = (RestaurantServiceInter) registry.lookup("restaurant");
            authService = (AuthServiceInter) registry.lookup("auth");
            reviewService = (ReviewServiceInter) registry.lookup("review");
            favouriteService = (FavouriteServiceInter) registry.lookup("favourite");
            IO.printSuccessMessage("RMI connection established.");
        } catch (Exception ignored) {
            try {
                Registry registry = LocateRegistry.getRegistry(hostname, rmiPort);

                restaurantService = (RestaurantServiceInter) registry.lookup("restaurant");
                authService = (AuthServiceInter) registry.lookup("auth");
                reviewService = (ReviewServiceInter) registry.lookup("review");
                favouriteService = (FavouriteServiceInter) registry.lookup("favourite");
                IO.printSuccessMessage("RMI connection established.");
            } catch (Exception e) {
                System.err.println("WARNING: RMI lookup failed: " + e.getMessage());
            }
        }
    }

    /**
     * Ripunta la cache locale di ogni DAO alla cartella dell'utente autenticato,
     * cosi' che i dati di sessioni diverse non si mescolino sullo stesso client.
     */
    public void switchUser(UUID userId) {
        customerDAO.repointTo(userId);
        ownerDAO.repointTo(userId);
        restaurantDAO.repointTo(userId);
        reviewDAO.repointTo(userId);
        locationDAO.repointTo(userId);
    }
}
