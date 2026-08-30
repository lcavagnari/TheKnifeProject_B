package it.uninsubria.laboratoriob.server.remote;

import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.UUID;

/** Implementazione RMI del servizio preferiti (solo cache, delega al store). */
public class FavouriteServiceImpl extends UnicastRemoteObject implements FavouriteServiceInter {
    private final ServerDataStore store;

    /**
     * Costruisce l'implementazione remota del servizio preferiti.
     *
     * @param store facade dei dati server
     * @throws RemoteException se l'esportazione RMI fallisce
     */
    public FavouriteServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public boolean addFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        if (userID == null || restaurantId == null) return false;
        if (store.users().addFavourite(userID, restaurantId)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean removeFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        if (userID == null || restaurantId == null) return false;
        if (store.users().removeFavourite(userID, restaurantId)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public Set<UUID> findFavourites(UUID userID) throws RemoteException {
        return store.users().findFavourites(userID);
    }
}
