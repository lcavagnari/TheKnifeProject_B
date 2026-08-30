package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.objects.Review;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Servizio RMI per la gestione delle recensioni.
 * <p>
 * Fornisce operazioni CRUD, interrogazione per ristorante/utente
 * e gestione delle risposte del proprietario.
 */
public interface ReviewServiceInter extends Remote {
    /**
     * Restituisce tutte le recensioni di un ristorante.
     *
     * @param restaurantId UUID del ristorante
     * @return lista delle recensioni
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException;

    /**
     * Restituisce tutte le recensioni scritte da un utente.
     *
     * @param userId UUID dell'utente
     * @return lista delle recensioni
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public List<Review> findByUser(UUID userId) throws RemoteException;

    /**
     * Restituisce tutte le recensioni del sistema.
     *
     * @return lista completa delle recensioni
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public List<Review> findAll() throws RemoteException;

    /**
     * Restituisce un insieme paginato di tutte le recensioni.
     *
     * @param offset indice di partenza (0-based)
     * @param limit  numero massimo di elementi
     * @return insieme delle recensioni nella finestra richiesta
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public Set<Review> findAll(int offset, int limit) throws RemoteException;

    /**
     * Salva una nuova recensione.
     *
     * @param review recensione da salvare
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public boolean save(Review review) throws RemoteException;

    /**
     * Aggiorna una recensione esistente.
     *
     * @param review recensione con i dati aggiornati
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public boolean update(Review review) throws RemoteException;

    /**
     * Elimina una recensione.
     *
     * @param id UUID della recensione da eliminare
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public boolean delete(UUID id) throws RemoteException;

    /**
     * Aggiunge una risposta del proprietario a una recensione.
     *
     * @param reviewId UUID della recensione
     * @param reply    testo della risposta
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public boolean replyToReview(UUID reviewId, String reply) throws RemoteException;
}
