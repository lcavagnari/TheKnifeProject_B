package it.uninsubria.laboratoriob.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

/**
 * Servizio RMI per la gestione dei preferiti degli utenti.
 * <p>
 * Permette di aggiungere, rimuovere e interrogare i ristoranti
 * preferiti di un proprietario.
 */
public interface FavouriteServiceInter extends Remote {
    /**
     * Aggiunge un ristorante ai preferiti di un proprietario.
     *
     * @param ownerId      UUID del proprietario
     * @param restaurantId UUID del ristorante
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public boolean addFavourites(UUID ownerId, UUID restaurantId) throws RemoteException;

    /**
     * Rimuove un ristorante dai preferiti di un proprietario.
     *
     * @param ownerId      UUID del proprietario
     * @param restaurantId UUID del ristorante
     * @return {@code true} se l'operazione ha successo
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public boolean removeFavourites(UUID ownerId, UUID restaurantId) throws RemoteException;

    /**
     * Restituisce l'insieme degli UUID dei ristoranti preferiti di un proprietario.
     *
     * @param ownerId UUID del proprietario
     * @return insieme degli UUID dei ristoranti preferiti
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    public Set<UUID> findFavourites(UUID ownerId) throws RemoteException;
}
