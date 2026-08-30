package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DAO per la gestione degli utenti di tipo {@link Customer}.
 * Estende {@link JsonUserDAO} gestendo la persistenza locale della lista dei
 * ristoranti preferiti e delegando le operazioni RMI al server.
 */
public final class JsonCustomerDAO extends JsonUserDAO<Customer> {

    private volatile FavouriteServiceInter favService;
    private File favouritesFile;

    /**
     * Costruisce un nuovo DAO per i clienti.
     *
     * @param authService servizio di autenticazione RMI
     * @param favService  servizio dei preferiti RMI
     */
    JsonCustomerDAO(AuthServiceInter authService, FavouriteServiceInter favService) {
        super(Customer.class, UserRole.CLIENT, authService);

        this.favService = favService;
        this.favouritesFile = new File(Constants.ROOT, "favourites.json");
    }

    /** {@inheritDoc} */
    @Override
    public void repointTo(UUID userId) {
        super.repointTo(userId);
        File userDir = new File(Constants.ROOT, userId.toString());
        this.favouritesFile = new File(userDir, favouritesFile.getName());
    }

    private Set<UUID> loadFavouritesFromDisk() {
        if (!favouritesFile.exists()) return new HashSet<>();
        try {
            JsonNode node = mapper.readTree(favouritesFile);
            if (!node.isArray()) return new HashSet<>();
            Set<UUID> ids = new HashSet<>();
            for (JsonNode n : node) ids.add(UUID.fromString(n.asText()));
            return ids;
        } catch (IOException e) {
            System.err.println("Errore loadFavouritesFromDisk in JsonCustomerDAO: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private void persistFavourites(Set<UUID> favouriteIds) {
        try {
            if (!favouritesFile.getParentFile().exists()) favouritesFile.getParentFile().mkdirs();
            ArrayNode array = mapper.createArrayNode();
            for (UUID id : favouriteIds) array.add(id.toString());
            File tmp = File.createTempFile("favourites_", ".json", favouritesFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), favouritesFile.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistFavourites in JsonCustomerDAO: " + e.getMessage());
        }
    }

    void setRemoteFavService(FavouriteServiceInter favService) {
        this.favService = favService;
    }

    private FavouriteServiceInter ensureFavService() {
        FavouriteServiceInter current = favService;
        if (current != null) return current;
        FavouriteServiceInter fresh = RmiRepository.lookupFavouriteService();
        if (fresh != null) this.favService = fresh;
        return fresh;
    }

    /** {@inheritDoc} */
    @Override
    protected Customer mapNode(JsonNode node) {
        Set<UUID> favourites = loadFavouritesFromDisk();

        return new Customer(
                readId(node),
                readString(node, "username"),
                readString(node, "passwordHash"),
                readString(node, "passwordSalt"),
                readString(node, "name"),
                readString(node, "lastName"),
                readLocation(node),
                readDate(node),
                favourites,
                readBoolean(node, "system", false)
        );
    }

    /** {@inheritDoc} */
    @Override
    protected ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();

        for (Customer c : cacheById.values()) {
            ObjectNode node = mapper.createObjectNode();
            writeUserFields(node, c);
            array.add(node);
        }

        return array;
    }

    /**
     * Aggiunge un ristorante alla lista dei preferiti del cliente.
     * Registra l'associazione sia localmente che tramite il servizio RMI.
     *
     * @param customerId   ID del cliente
     * @param restaurantId ID del ristorante da aggiungere ai preferiti
     * @return {@code true} se il ristorante è stato aggiunto con successo
     * @throws ServiceUnavailableException se il server RMI non è raggiungibile
     */
    public boolean addFavourite(UUID customerId, UUID restaurantId) {
        Customer customer = cacheById.get(customerId);
        if (customer == null) return false;

        FavouriteServiceInter svc = ensureFavService();
        if (svc != null) {
            try {
                svc.addFavourites(customerId, restaurantId);
            } catch (RemoteException e) {
                this.favService = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }

        customer.getFavouriteRestourants().add(restaurantId);
        persistFavourites(customer.getFavouriteRestourants());
        return true;
    }

    /**
     * Rimuove un ristorante dalla lista dei preferiti del cliente.
     * Elimina l'associazione sia localmente che tramite il servizio RMI.
     *
     * @param customerId   ID del cliente
     * @param restaurantId ID del ristorante da rimuovere dai preferiti
     * @return {@code true} se il ristorante è stato rimosso con successo
     * @throws ServiceUnavailableException se il server RMI non è raggiungibile
     */
    public boolean removeFavourite(UUID customerId, UUID restaurantId) {
        Customer customer = cacheById.get(customerId);
        if (customer == null) return false;

        FavouriteServiceInter svc = ensureFavService();
        if (svc != null) {
            try {
                svc.removeFavourites(customerId, restaurantId);
            } catch (RemoteException e) {
                this.favService = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }

        customer.getFavouriteRestourants().remove(restaurantId);
        persistFavourites(customer.getFavouriteRestourants());
        return true;
    }

    /**
     * Restituisce l'insieme degli ID dei ristoranti preferiti dal cliente.
     *
     * @param customerId ID del cliente
     * @return insieme immutabile di ID dei ristoranti preferiti, o un insieme vuoto se il cliente non esiste
     */
    public Set<UUID> findFavourites(UUID customerId) {
        Customer customer = cacheById.get(customerId);
        return customer != null ? Set.copyOf(customer.getFavouriteRestourants()) : Set.of();
    }
}
