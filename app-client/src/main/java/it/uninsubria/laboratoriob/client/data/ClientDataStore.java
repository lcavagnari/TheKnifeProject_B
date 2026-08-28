package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import lombok.Getter;

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

    public ClientDataStore(RestaurantServiceInter restaurantService,
                           AuthServiceInter authService,
                           ReviewServiceInter reviewService,
                           FavouriteServiceInter favouriteService) {
        this.restaurantService = restaurantService;
        this.authService = authService;
        this.reviewService = reviewService;
        this.favouriteService = favouriteService;
        this.customerDAO = new JsonCustomerDAO(authService,favouriteService);
        this.ownerDAO = new JsonOwnerDAO(authService, restaurantService);
        this.restaurantDAO = new JsonRestaurantDAO(restaurantService);
        this.locationDAO = new JsonLocationDAO();
        this.reviewDAO = new JsonReviewDAO(customerDAO, reviewService);
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
