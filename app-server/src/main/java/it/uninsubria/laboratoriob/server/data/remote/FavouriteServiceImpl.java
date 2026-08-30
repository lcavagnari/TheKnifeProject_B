package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.data.dao.CustomerDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FavouriteServiceImpl extends UnicastRemoteObject implements FavouriteServiceInter {

    private final CustomerDAO cDAO = new CustomerDAO();
    private final ServerDataStore store;

    public FavouriteServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public boolean addFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        if (userID == null || restaurantId == null) return false;

        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> cDAO.addFavourites(userID, restaurantId))
                .exceptionally(ex -> false);

        boolean ok = dbOk.join();
        if (ok) {
            User u = store.users().findById(userID);
            if (u instanceof Customer c) c.getFavouriteRestourants().add(restaurantId);
            else throw new RemoteException("Unsupported operation");

        } else throw new RemoteException("Error occured while saving changes");

        return ok;
    }

    @Override
    public boolean removeFavourites(UUID userID, UUID restaurantId) throws RemoteException {
        if (userID == null || restaurantId == null) return false;

        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> cDAO.removeFavourites(userID, restaurantId))
                .exceptionally(ex -> false);

        boolean ok = dbOk.join();
        if (ok) {
            User u = store.users().findById(userID);
            if (u instanceof Customer c) c.getFavouriteRestourants().remove(restaurantId);
            else throw new RemoteException("Unsupported operation");

        } else throw new RemoteException("Error occured while saving changes");

        return ok;
    }

    @Override
    public Set<UUID> findFavourites(UUID userID) throws RemoteException {
        if (userID == null) return Set.of();

        Set<UUID> merged = new HashSet<>();
        User u = store.users().findById(userID);
        if (u instanceof Customer c) merged.addAll(c.getFavouriteRestourants());

        CompletableFuture<Set<UUID>> db = CompletableFuture
                .supplyAsync(() -> cDAO.findFavourites(userID))
                .exceptionally(ex -> Set.of());
        merged.addAll(db.join());

        return merged;
    }
}
