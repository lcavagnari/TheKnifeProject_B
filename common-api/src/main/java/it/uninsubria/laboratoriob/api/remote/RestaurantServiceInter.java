package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

/**
 * Servizio RMI per la gestione dei ristoranti.
 * <p>
 * Fornisce operazioni CRUD, interrogazione e gestione della relazione
 * proprietario-ristorante, incluse le operazioni sulle cucine e sui servizi.
 */
public interface RestaurantServiceInter extends Remote {
    /**
     * Restituisce un insieme paginato di tutti i ristoranti.
     *
     * @param offset indice di partenza (0-based)
     * @param limit  numero massimo di elementi da restituire
     * @return insieme dei ristoranti nella finestra richiesta
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    Set<Restaurant> findAll(int offset, int limit) throws RemoteException;

    /**
     * Restituisce il numero totale di ristoranti registrati.
     *
     * @return conteggio totale
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    long count() throws RemoteException;

    /**
     * Cerca un ristorante per il suo identificativo.
     *
     * @param id UUID del ristorante
     * @return il ristorante trovato, oppure {@code null} se inesistente
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    Restaurant findById(UUID id) throws RemoteException;

    /**
     * Restituisce tutti i ristoranti di un determinato proprietario.
     *
     * @param id UUID del proprietario
     * @return insieme dei ristoranti posseduti
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    Set<Restaurant> findByOwner(UUID id) throws  RemoteException;

    /**
     * Salva un nuovo ristorante nel sistema.
     *
     * @param restaurant ristorante da salvare
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean save(Restaurant restaurant) throws RemoteException;

    /**
     * Aggiorna un ristorante esistente.
     *
     * @param restaurant ristorante con i dati aggiornati
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean update(Restaurant restaurant) throws RemoteException;

    /**
     * Elimina un ristorante dal sistema.
     *
     * @param id UUID del ristorante da eliminare
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean delete(UUID id) throws RemoteException;

    /**
     * Assegna un proprietario a un ristorante.
     *
     * @param restaurant ristorante da associare
     * @param ownerId    UUID del proprietario
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean registerOwner(Restaurant restaurant, UUID ownerId) throws RemoteException;

    /**
     * Rimuove la relazione proprietario-ristorante.
     *
     * @param ownerId      UUID del proprietario
     * @param restaurantId UUID del ristorante
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean unregisterOwner(UUID ownerId, UUID restaurantId) throws RemoteException;

    /**
     * Aggiorna l'insieme dei tipi di cucina di un ristorante.
     *
     * @param restaurantId UUID del ristorante
     * @param cuisines     nuovi tipi di cucina
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException;

    /**
     * Aggiorna l'insieme dei servizi offerti da un ristorante.
     *
     * @param restaurantId UUID del ristorante
     * @param services     nuovi servizi
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException;
}
