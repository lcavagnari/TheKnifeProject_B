package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;
import lombok.Getter;

import java.util.Set;
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

    public ClientDataStore() {
        this.customerDAO = new JsonCustomerDAO(null, null);
        this.restaurantDAO = new JsonRestaurantDAO(null);
        this.ownerDAO = new JsonOwnerDAO(null, null, restaurantDAO);
        this.reviewDAO = new JsonReviewDAO(customerDAO, null);
    }

    /**
     * Acquires all RMI stubs (via {@link RmiRepository}, which must already be
     * {@code configure()}d) and pushes the ones that succeeded onto the DAOs.
     * Safe to call again later (e.g. after a manual reconnect).
     */
    public boolean acquireRemoteServices() {
        Set<String> acquired = RmiRepository.acquireAll();
        propagateServices();

        if (acquired.isEmpty()) {
            System.err.println("WARNING: RMI connection unavailable, running in local-only mode.");
        } else {
            IO.printSuccessMessage("RMI connection established.");
        }
        return !acquired.isEmpty();
    }

    private void propagateServices() {
        customerDAO.setRemoteAuthService(RmiRepository.getAuthService());
        customerDAO.setRemoteFavService(RmiRepository.getFavouriteService());
        ownerDAO.setRemoteAuthService(RmiRepository.getAuthService());
        ownerDAO.setRemoteRestaurantService(RmiRepository.getRestaurantService());
        restaurantDAO.setRemoteRestaurantService(RmiRepository.getRestaurantService());
        reviewDAO.setRemoteReviewService(RmiRepository.getReviewService());
    }

    /**
     * Ripunta la cache locale dei DAO utente (profilo, ristoranti posseduti, preferiti)
     * alla cartella dell'utente autenticato. Ristoranti e recensioni restano globali:
     * sono dati di consultazione condivisi, non legati a un singolo utente.
     */
    public void switchUser(UUID userId) {
        customerDAO.repointTo(userId);
        ownerDAO.repointTo(userId);
    }
}
