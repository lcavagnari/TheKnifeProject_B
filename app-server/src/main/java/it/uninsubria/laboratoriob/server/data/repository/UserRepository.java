package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.server.data.dao.CustomerDAO;
import it.uninsubria.laboratoriob.server.data.dao.OwnerDAO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OwnerDAO ownerDAO = new OwnerDAO();
    private final Map<UUID, User> byId = new ConcurrentHashMap<>();
    private final Map<String, User> byName = new ConcurrentHashMap<>();

    // ── Write operations (DB + cache) ──

    public boolean saveCustomer(Customer c) {
        boolean ok = customerDAO.save(c);
        if (ok) putCache(c);
        return ok;
    }

    public boolean saveOwner(Owner o) {
        boolean ok = ownerDAO.save(o);
        if (ok) putCache(o);
        return ok;
    }

    public boolean updateCustomer(Customer c) {
        boolean ok = customerDAO.update(c);
        if (ok) putCache(c);
        return ok;
    }

    public boolean updateOwner(Owner o) {
        boolean ok = ownerDAO.update(o);
        if (ok) putCache(o);
        return ok;
    }

    public boolean delete(UUID id) {
        User removed = byId.remove(id);
        if (removed != null) byName.remove(removed.getUsername());
        if (removed instanceof Customer) return customerDAO.delete(id);
        if (removed instanceof Owner) return ownerDAO.delete(id);
        return false;
    }

    // ── Favourite operations ──

    public boolean addFavourite(UUID userId, UUID restaurantId) {
        boolean ok = customerDAO.addFavourites(userId, restaurantId);
        if (ok) {
            User u = byId.get(userId);
            if (u instanceof Customer c) c.getFavouriteRestourants().add(restaurantId);
        }
        return ok;
    }

    public boolean removeFavourite(UUID userId, UUID restaurantId) {
        boolean ok = customerDAO.removeFavourites(userId, restaurantId);
        if (ok) {
            User u = byId.get(userId);
            if (u instanceof Customer c) c.getFavouriteRestourants().remove(restaurantId);
        }
        return ok;
    }

    public Set<UUID> findFavourites(UUID userId) {
        User u = byId.get(userId);
        if (u instanceof Customer c) return Set.copyOf(c.getFavouriteRestourants());
        return customerDAO.findFavourites(userId);
    }

    // ── Read operations (cache) ──

    public User findById(UUID id) { return byId.get(id); }

    public User findByName(String name) { return byName.get(name); }

    public boolean hasByName(String name) { return byName.containsKey(name); }

    public long count() { return byId.size(); }

    // ── Cache-only operations (for Loader) ──

    public void putCache(User u) {
        byId.put(u.getId(), u);
        byName.put(u.getUsername(), u);
    }

    public void removeCache(UUID id) {
        User u = byId.remove(id);
        if (u != null) byName.remove(u.getUsername());
    }

    // ── DAO access (for Loader bulk load) ──

    public List<Owner> loadAllOwnersFromDb() { return ownerDAO.findAll(); }

    public List<Customer> loadAllCustomersFromDb() { return customerDAO.findAll(); }
}
