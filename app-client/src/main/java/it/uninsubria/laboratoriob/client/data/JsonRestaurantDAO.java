package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class JsonRestaurantDAO implements DAO<Restaurant> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private File storeFile;
    private final RestaurantServiceInter service;

    private final ConcurrentHashMap<UUID, Restaurant> cacheById = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonRestaurantDAO(RestaurantServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "restaurants.json");
        this.service = service;
    }

    public void repointTo(UUID userId) {
        File userDir = new File(Constants.ROOT, userId.toString());
        this.storeFile = new File(userDir, storeFile.getName());
        this.cacheLoaded = false;
        cacheById.clear();
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
            for (JsonNode n : (ArrayNode) node) {
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
        for (Restaurant r : cacheById.values()) {
            array.add(toNode(r));
        }
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

        Location loc = null;
        JsonNode locNode = node.path("location");
        if (!locNode.isMissingNode() && !locNode.isNull()) {
            loc = new Location(
                    Nation.fromString(locNode.path("nation").asText()),
                    locNode.path("city").asText(),
                    locNode.path("latitude").asDouble(),
                    locNode.path("longitude").asDouble(),
                    locNode.path("address").asText()
            );
        }

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
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        Set<String> services = new HashSet<>();
        JsonNode servicesNode = node.path("services");
        if (servicesNode.isArray()) {
            for (JsonNode s : servicesNode) {
                services.add(s.asText());
            }
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

        // TODO: extract this method to JsonLocationDao
        if (restaurant.getLocation() != null) {
            ObjectNode locNode = mapper.createObjectNode();
            locNode.put("nation", restaurant.getLocation().getNation().name());
            locNode.put("city", restaurant.getLocation().getCity());
            locNode.put("latitude", restaurant.getLocation().getLatitude());
            locNode.put("longitude", restaurant.getLocation().getLongitude());
            locNode.put("address", restaurant.getLocation().getAddress());
            node.set("location", locNode);
        }

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
        if (service != null) {
            try {
                Restaurant remote = service.findById(id);
                if (remote != null) {
                    cacheById.put(remote.getId(), remote);
                    persistAtomic(toArrayNode());
                    return Optional.of(remote);
                }
            } catch (RemoteException e) {
                System.err.println("RMI fallback findById restaurant: " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Restaurant> findAll() {
        ensureCacheLoaded();
        List<Restaurant> local = new ArrayList<>(cacheById.values());
        if (!local.isEmpty()) return local;
        if (service != null) {
            try {
                List<Restaurant> remote = service.findAll(0, 1000);
                for (Restaurant r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return new ArrayList<>(cacheById.values());
            } catch (RemoteException e) {
                System.err.println("RMI fallback findAll restaurant: " + e.getMessage());
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

    @Override
    public long count() {
        ensureCacheLoaded();
        if (!cacheById.isEmpty()) return cacheById.size();
        if (service != null) {
            try {
                return service.count();
            } catch (RemoteException e) {
                System.err.println("RMI fallback count restaurant: " + e.getMessage());
            }
        }
        return 0;
    }

    public List<Restaurant> findByOwner(UUID ownerId) {
        ensureCacheLoaded();
        List<Restaurant> local = cacheById.values().stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
        if (!local.isEmpty()) return local;
        if (service != null) {
            try {
                List<Restaurant> remote = service.findByOwner(ownerId);
                for (Restaurant r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return remote;
            } catch (RemoteException e) {
                System.err.println("RMI fallback findByOwner restaurant: " + e.getMessage());
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
        if (service != null) {
            try { service.save(restaurant); } catch (RemoteException e) {
                System.err.println("RMI sync save restaurant: " + e.getMessage());
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
        if (service != null) {
            try { service.update(restaurant); } catch (RemoteException e) {
                System.err.println("RMI sync update restaurant: " + e.getMessage());
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
        if (service != null) {
            try { service.delete(id); } catch (RemoteException e) {
                System.err.println("RMI sync delete restaurant: " + e.getMessage());
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
        if (service != null) {
            try { service.updateCuisines(restaurantId, cuisines); } catch (RemoteException e) {
                System.err.println("RMI sync updateCuisines: " + e.getMessage());
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
        if (service != null) {
            try { service.updateServices(restaurantId, services); } catch (RemoteException e) {
                System.err.println("RMI sync updateServices: " + e.getMessage());
            }
        }
        return true;
    }
}
