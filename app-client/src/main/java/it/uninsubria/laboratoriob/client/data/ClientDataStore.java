package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.objects.*;
import lombok.Getter;

/**
 * Facade che orchestra tutti i DAO JSON del client.
 * <p>
 * Fornisce accesso centralizzato a tutti i data access object per le entità del sistema.
 * I dati vengono persistiti localmente su file JSON, fungendo da cache lato client
 * dei dati del server.
 * </p>
 * <p>
 * Questa classe è progettata per essere facilmente sostituibile con un'implementazione
 * RMI in futuro: i DAO locali possono essere scelti con quelli remoti senza
 * modificare il codice che consuma i dati.
 * </p>
 */
@Getter
public class ClientDataStore {

    private final JsonCustomerDAO customerDAO;
    private final JsonOwnerDAO ownerDAO;
    private final JsonRestaurantDAO restaurantDAO;
    private final JsonReviewDAO reviewDAO;
    private final JsonLocationDAO locationDAO;

    public ClientDataStore() {
        this.customerDAO = new JsonCustomerDAO();
        this.ownerDAO = new JsonOwnerDAO();
        this.restaurantDAO = new JsonRestaurantDAO();
        this.locationDAO = new JsonLocationDAO();
        this.reviewDAO = new JsonReviewDAO(customerDAO);
    }
}
