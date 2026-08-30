package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.data.dao.CustomerDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.UUID;

public class FavouriteServiceImpl extends UnicastRemoteObject implements FavouriteServiceInter {

    private final CustomerDAO cDAO = new CustomerDAO();
    private final ServerDataStore store;

    public FavouriteServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public boolean addFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        boolean ok = cDAO.addFavourites(userID, restaurantId);

        if (ok) {
            User u = store.users().findById(userID);
            if (u instanceof Customer)
                ((Customer) u).getFavouriteRestourants().add(restaurantId);
        }
        return ok;
    }

    @Override
    public boolean removeFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        boolean ok = cDAO.removeFavourites(userID, restaurantId);

        if (ok) {
            User u = store.users().findById(userID);
            if (u instanceof Customer)
                ((Customer) u).getFavouriteRestourants().remove(restaurantId);
        }
        return ok;
    }

    @Override
    public Set<UUID> findFavourites(UUID userID) throws RemoteException {
        User u = store.users().findById(userID);

        if (u instanceof Customer)
            return Set.copyOf(((Customer) u).getFavouriteRestourants());

        return cDAO.findFavourites(userID);
    }
}
