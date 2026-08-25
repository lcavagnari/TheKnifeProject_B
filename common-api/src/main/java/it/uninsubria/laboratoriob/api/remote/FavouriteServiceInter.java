package it.uninsubria.laboratoriob.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

public interface FavouriteServiceInter extends Remote {
    public boolean addFavourites(UUID ownerId, UUID restaurantId) throws RemoteException;
    public boolean removeFavourites(UUID ownerId, UUID restaurantId) throws RemoteException;
    public Set<UUID> findFavourites(UUID ownerId) throws RemoteException;
}
