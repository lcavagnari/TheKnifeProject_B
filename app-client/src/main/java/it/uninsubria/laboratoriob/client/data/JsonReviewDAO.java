package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonReviewDAO implements DAO<Review> {

    // TODO: Ensure this one works only in regards to the restaurants.

    private static final ObjectMapper mapper = new ObjectMapper();
    private File storeFile;
    private final JsonCustomerDAO customerDAO;
    private final ReviewServiceInter service;

    private final ConcurrentHashMap<UUID, Review> cacheById = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonReviewDAO(JsonCustomerDAO customerDAO, ReviewServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "reviews.json");
        this.customerDAO = customerDAO;
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
                Review review = mapNode(n);
                cacheById.put(review.getId(), review);
            }
        } catch (IOException e) {
            System.err.println("Errore loadFromDisk in JsonReviewDAO: " + e.getMessage());
        }
    }

    private void persistAtomic(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            File tmp = File.createTempFile("reviews_", ".json", storeFile.getParentFile());
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, array);
            Files.move(tmp.toPath(), storeFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Errore persistAtomic in JsonReviewDAO: " + e.getMessage());
        }
    }

    private ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Review review : cacheById.values()) {
            array.add(toNode(review));
        }
        return array;
    }

    private Review mapNode(JsonNode node) {
        UUID id = UUID.fromString(node.path("id").asText());
        UUID restaurantId = UUID.fromString(node.path("restaurantId").asText());
        UUID userId = UUID.fromString(node.path("userId").asText());
        int value = node.path("value").asInt();
        String text = node.path("text").asText("");
        String reply = node.path("reply").asText(null);

        String tsStr = node.path("timestamp").asText();
        LocalDateTime timestamp = tsStr != null && !tsStr.isEmpty()
                ? LocalDateTime.parse(tsStr) : LocalDateTime.now();

        User user = customerDAO.findById(userId).orElse(null);

        return new Review(id, null, user, value, timestamp, text, reply);
    }

    private ObjectNode toNode(Review review) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", review.getId().toString());
        node.put("restaurantId", review.getRestaurant() != null ? review.getRestaurant().getId().toString() : "");
        node.put("userId", review.getUser() != null ? review.getUser().getId().toString() : "");
        node.put("value", review.getValue());
        node.put("text", review.getText() != null ? review.getText() : "");
        node.put("reply", review.getReply());
        node.put("timestamp", review.getTimestamp() != null ? review.getTimestamp().toString() : "");
        return node;
    }

    @Override
    public Optional<Review> findById(UUID id) {
        ensureCacheLoaded();
        Review cached = cacheById.get(id);
        if (cached != null) return Optional.of(cached);
        return Optional.empty();
    }

    @Override
    public List<Review> findAll() {
        ensureCacheLoaded();
        List<Review> local = new ArrayList<>(cacheById.values());
        if (!local.isEmpty()) return local;
        if (service != null) {
            try {
                List<Review> remote = service.findAll();
                for (Review r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return new ArrayList<>(cacheById.values());
            } catch (RemoteException e) {
                System.err.println("RMI fallback findAll review: " + e.getMessage());
            }
        }
        return local;
    }

    @Override
    public List<Review> findAll(int offset, int limit) {
        List<Review> all = findAll();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() {
        ensureCacheLoaded();
        return cacheById.size();
    }

    public List<Review> findByRestaurant(UUID restaurantId) {
        ensureCacheLoaded();
        List<Review> local = cacheById.values().stream()
                .filter(r -> r.getRestaurant() != null && r.getRestaurant().getId().equals(restaurantId))
                .toList();
        if (!local.isEmpty()) return local;
        if (service != null) {
            try {
                List<Review> remote = service.findByRestaurant(restaurantId);
                for (Review r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return remote;
            } catch (RemoteException e) {
                System.err.println("RMI fallback findByRestaurant review: " + e.getMessage());
            }
        }
        return local;
    }

    public List<Review> findByUser(UUID userId) {
        ensureCacheLoaded();
        List<Review> local = cacheById.values().stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
                .toList();
        if (!local.isEmpty()) return local;
        if (service != null) {
            try {
                List<Review> remote = service.findByUser(userId);
                for (Review r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return remote;
            } catch (RemoteException e) {
                System.err.println("RMI fallback findByUser review: " + e.getMessage());
            }
        }
        return local;
    }

    @Override
    public boolean save(Review review) {
        if (review == null) return false;
        ensureCacheLoaded();
        if (cacheById.containsKey(review.getId())) return false;
        cacheById.put(review.getId(), review);
        persistAtomic(toArrayNode());
        if (service != null) {
            try { service.save(review); } catch (RemoteException e) {
                System.err.println("RMI sync save review: " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public boolean update(Review review) {
        if (review == null) return false;
        ensureCacheLoaded();
        if (!cacheById.containsKey(review.getId())) return false;
        cacheById.put(review.getId(), review);
        persistAtomic(toArrayNode());
        if (service != null) {
            try { service.update(review); } catch (RemoteException e) {
                System.err.println("RMI sync update review: " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        ensureCacheLoaded();
        Review removed = cacheById.remove(id);
        if (removed == null) return false;
        persistAtomic(toArrayNode());
        if (service != null) {
            try { service.delete(id); } catch (RemoteException e) {
                System.err.println("RMI sync delete review: " + e.getMessage());
            }
        }
        return true;
    }
}
