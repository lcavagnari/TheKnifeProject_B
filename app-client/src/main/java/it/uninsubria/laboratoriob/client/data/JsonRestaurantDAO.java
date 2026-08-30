package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class JsonRestaurantDAO implements DAO<Restaurant> {

    // TODO: fix Arraylist is not serialisable issue and ensure data is cached appropriately.

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;
    private volatile RestaurantServiceInter service;

    private final ConcurrentHashMap<UUID, Restaurant> cacheById = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonRestaurantDAO(RestaurantServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "restaurants.json");
        this.service = service;
    }

    void setRemoteRestaurantService(RestaurantServiceInter service) {
        this.service = service;
    }

    private RestaurantServiceInter ensureService() {
        RestaurantServiceInter current = service;
        if (current != null) return current;
        RestaurantServiceInter fresh = RmiRepository.lookupRestaurantService();
        if (fresh != null) this.service = fresh;
        return fresh;
    }

    private void ensureCacheLoaded() {
        if (cacheLoaded) return;

        synchronized (this) {
            if (cacheLoaded) return;
            loadFromDisk();

            cacheLoaded = true;
        }
    }

    private void loadFromDisk() {
        cacheById.clear();
        if (!storeFile.exists()) return;

        try {
            JsonNode node = mapper.readTree(storeFile);

            if (!node.isArray()) return;

            for (JsonNode n : node) {
                Restaurant r = mapNode(n);
                cacheById.put(r.getId(), r);
            }
        } catch (IOException e) {
            System.err.println("Errore loadFromDisk in JsonRestaurantDAO: " + e.getMessage());
        }
    }

    private void persistAtomic(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            File tmp = File.createTempFile("restaurants_", ".json", storeFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), storeFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistAtomic in JsonRestaurantDAO: " + e.getMessage());
        }
    }

    private ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Restaurant r : cacheById.values()) array.add(toNode(r));

        return array;
    }

    private Restaurant mapNode(JsonNode node) {
        UUID id = UUID.fromString(node.path("id").asText());
        String name = node.path("name").asText();
        String description = node.path("description").asText("");
        String websiteUrl = node.path("websiteUrl").asText("");
        String phone = node.path("phone").asText("");

        Owner owner = null;
        JsonNode ownerNode = node.path("owner");
        if (!ownerNode.isMissingNode() && !ownerNode.isNull() && !ownerNode.asText("").isEmpty()) {
            UUID ownerId = UUID.fromString(ownerNode.asText());
            owner = new Owner(ownerId, "", "", "", "", "", null, LocalDate.MIN);
        }

        Location loc = LocationMapper.fromNode(node.path("location"));

        PriceRange priceRange = PriceRange.MODERATE;
        JsonNode prNode = node.path("priceRange");
        if (!prNode.isMissingNode() && !prNode.isNull()) {
            try {
                priceRange = PriceRange.valueOf(prNode.asText());
            } catch (IllegalArgumentException ignored) {
                String symbol = prNode.asText("");
                priceRange = PriceRange.byDollarAmount(symbol.length());
            }
        }

        Award award = Award.fromInt(node.path("award").asInt(0));
        boolean greenStar = node.path("greenStar").asBoolean(false);
        boolean hasDelivery = node.path("hasDelivery").asBoolean(false);
        boolean hasOnlineBooking = node.path("hasOnlineBooking").asBoolean(false);

        Set<CuisineType> cuisinesTypes = new HashSet<>();
        JsonNode cuisinesNode = node.path("cuisinesTypes");
        if (cuisinesNode.isArray()) {
            for (JsonNode c : cuisinesNode) {
                try {
                    cuisinesTypes.add(CuisineType.valueOf(c.asText()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        Set<String> services = new HashSet<>();
        JsonNode servicesNode = node.path("services");
        if (servicesNode.isArray()) {
            for (JsonNode s : servicesNode) services.add(s.asText());
        }

        return new Restaurant(id, name, description, websiteUrl, owner, phone, loc,
                priceRange, hasDelivery, hasOnlineBooking, award, greenStar, cuisinesTypes, services);
    }

    private ObjectNode toNode(Restaurant restaurant) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", restaurant.getId().toString());
        node.put("name", restaurant.getName());
        node.put("description", restaurant.getDescription() != null ? restaurant.getDescription() : "");
        node.put("websiteUrl", restaurant.getWebsiteUrl() != null ? restaurant.getWebsiteUrl() : "");
        node.put("phone", restaurant.getPhone() != null ? restaurant.getPhone() : "");
        node.put("owner", restaurant.getOwner() != null ? restaurant.getOwner().getId().toString() : "");
        node.put("award", restaurant.getAward().getValue());
        node.put("greenStar", restaurant.isGreenStar());
        node.put("hasDelivery", restaurant.isHasDelivery());
        node.put("hasOnlineBooking", restaurant.isHasOnlineBooking());
        node.put("priceRange", restaurant.getPriceRange().name());

        if (restaurant.getLocation() != null)
            node.set("location", LocationMapper.toNode(mapper, restaurant.getLocation()));


        ArrayNode cuisinesArray = mapper.createArrayNode();
        restaurant.getCuisinesTypes().forEach(c -> cuisinesArray.add(c.name()));
        node.set("cuisinesTypes", cuisinesArray);

        ArrayNode servicesArray = mapper.createArrayNode();
        restaurant.getServices().forEach(servicesArray::add);
        node.set("services", servicesArray);

        return node;
    }

    @Override
    public Optional<Restaurant> findById(UUID id) {
        ensureCacheLoaded();

        Restaurant cached = cacheById.get(id);
        if (cached != null) return Optional.of(cached);

        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try {
                Restaurant remote = svc.findById(id);
                if (remote != null) {
                    cacheById.put(remote.getId(), remote);
                    persistAtomic(toArrayNode());
                    return Optional.of(remote);
                }
            } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Restaurant> findAll() {
        ensureCacheLoaded();

        List<Restaurant> local = new ArrayList<>(cacheById.values());

        // TODO: the fact that cache isnt empty doesnt mean that it is up to date.
        if (!local.isEmpty() && countLocal() == countRemote()) return local;
        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try {
                Set<Restaurant> remote = svc.findAll(0, 1000);

                for (Restaurant r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());

                return new ArrayList<>(cacheById.values());
            } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return local;
    }

    @Override
    public List<Restaurant> findAll(int offset, int limit) {
        List<Restaurant> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    public long countRemote() {
        RestaurantServiceInter svc = ensureService();
        if (svc == null) return 0;
        try {
            return svc.count();
        } catch (RemoteException e) {
            this.service = null;
            throw new ServiceUnavailableException("Server non disponibile", e);
        }
    }

    public long countLocal() {
        ensureCacheLoaded();
        return (!cacheById.isEmpty()) ? cacheById.size() : 0;
    }

    @Override
    public long count() {
        long local = countLocal();
        long remote = countRemote();

        return local+remote;
    }

    public List<Restaurant> findByOwner(UUID ownerId) {
        ensureCacheLoaded();
        List<Restaurant> local = cacheById.values().stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
        if (!local.isEmpty()) return local;
        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try {
                Set<Restaurant> remote = svc.findByOwner(ownerId);
                for (Restaurant r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return new ArrayList<>(remote);
            } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return local;
    }

    @Override
    public boolean save(Restaurant restaurant) {
        if (restaurant == null) return false;
        ensureCacheLoaded();
        if (cacheById.containsKey(restaurant.getId())) return false;
        cacheById.put(restaurant.getId(), restaurant);
        persistAtomic(toArrayNode());
        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.save(restaurant); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }

    @Override
    public boolean update(Restaurant restaurant) {
        if (restaurant == null) return false;

        ensureCacheLoaded();
        if (!cacheById.containsKey(restaurant.getId())) return false;

        cacheById.put(restaurant.getId(), restaurant);
        persistAtomic(toArrayNode());

        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.update(restaurant); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        ensureCacheLoaded();
        Restaurant removed = cacheById.remove(id);
        if (removed == null) return false;
        persistAtomic(toArrayNode());
        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.delete(id); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }

    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) {
        ensureCacheLoaded();
        Restaurant r = cacheById.get(restaurantId);
        if (r == null) return false;
        r.getCuisinesTypes().clear();
        r.getCuisinesTypes().addAll(cuisines);
        persistAtomic(toArrayNode());
        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.updateCuisines(restaurantId, cuisines); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }

    public boolean updateServices(UUID restaurantId, Set<String> services) {
        ensureCacheLoaded();
        Restaurant r = cacheById.get(restaurantId);
        if (r == null) return false;
        r.getServices().clear();
        r.getServices().addAll(services);
        persistAtomic(toArrayNode());
        RestaurantServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.updateServices(restaurantId, services); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }
}
