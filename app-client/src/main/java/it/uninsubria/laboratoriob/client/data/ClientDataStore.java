package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import lombok.Getter;

/**
 * Facade che orchestra tutti i DAO del client.
 * <p>
 * Fornisce accesso centralizzato a tutti i data access object per le entità del sistema.
 * I dati vengono persistiti localmente su file JSON, fungendo da cache lato client
 * dei dati del server.
 * </p>
 * <p>
 * Quando il server RMI è disponibile, gli stub remoti vengono iniettati
 * tramite costruttore e resi accessibili tramite i relativi campi.
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
        this.customerDAO = new JsonCustomerDAO();
        this.ownerDAO = new JsonOwnerDAO();
        this.restaurantDAO = new JsonRestaurantDAO();
        this.locationDAO = new JsonLocationDAO();
        this.reviewDAO = new JsonReviewDAO(customerDAO);
        this.restaurantService = restaurantService;
        this.authService = authService;
        this.reviewService = reviewService;
        this.favouriteService = favouriteService;
    }
}
