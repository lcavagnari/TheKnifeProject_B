package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.server.data.CustomerDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.UUID;

public class FavouriteServiceImpl extends UnicastRemoteObject implements FavouriteServiceInter {
    private final CustomerDAO cDAO = new CustomerDAO();

    protected FavouriteServiceImpl() throws RemoteException {
    }

    @Override
    public boolean addFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        return cDAO.addFavourites(userID, restaurantId);
    }

    @Override
    public boolean removeFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        return cDAO.removeFavourites(userID, restaurantId);
    }

    @Override
    public Set<UUID> findFavourites(UUID userID) throws RemoteException {
        return cDAO.findFavourites(userID);
    }
}
