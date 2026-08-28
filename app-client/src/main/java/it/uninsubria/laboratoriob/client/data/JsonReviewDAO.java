package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;

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

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;
    private final JsonCustomerDAO customerDAO;
    private volatile ReviewServiceInter service;

    private final ConcurrentHashMap<UUID, Review> cacheById = new ConcurrentHashMap<>();
    private volatile boolean cacheLoaded = false;

    public JsonReviewDAO(JsonCustomerDAO customerDAO, ReviewServiceInter service) {
        this.storeFile = new File(Constants.ROOT, "reviews.json");
        this.customerDAO = customerDAO;
        this.service = service;
    }

    void setRemoteReviewService(ReviewServiceInter service) {
        this.service = service;
    }

    private ReviewServiceInter ensureService() {
        ReviewServiceInter current = service;
        if (current != null) return current;
        ReviewServiceInter fresh = RmiRepository.lookupReviewService();
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

        Review review = new Review(id, null, user, value, timestamp, text, reply);

        String respondedAtStr = node.path("respondedAt").asText(null);
        if (respondedAtStr != null && !respondedAtStr.isEmpty())
            review.setRespondedAt(LocalDateTime.parse(respondedAtStr));

        return review;
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
        node.put("respondedAt", review.getRespondedAt() != null ? review.getRespondedAt().toString() : "");
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
        ReviewServiceInter svc = ensureService();
        if (svc != null) {
            try {
                List<Review> remote = svc.findAll();
                for (Review r : remote) cacheById.put(r.getId(), r);
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
        ReviewServiceInter svc = ensureService();
        if (svc != null) {
            try {
                List<Review> remote = svc.findByRestaurant(restaurantId);
                for (Review r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return remote;
            } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
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
        ReviewServiceInter svc = ensureService();
        if (svc != null) {
            try {
                List<Review> remote = svc.findByUser(userId);
                for (Review r : remote) cacheById.put(r.getId(), r);
                persistAtomic(toArrayNode());
                return remote;
            } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
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
        ReviewServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.save(review); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
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
        ReviewServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.update(review); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }

    public boolean replyToReview(Review review, String reply) {
        if (review == null || reply == null || reply.isBlank()) return false;
        ensureCacheLoaded();

        ReviewServiceInter svc = ensureService();
        boolean synced;
        try {
            synced = svc != null && svc.replyToReview(review.getId(), reply);
        } catch (RemoteException e) {
            this.service = null;
            throw new ServiceUnavailableException("Server non disponibile", e);
        }
        if (!synced) return false;

        review.setReply(reply);
        review.setRespondedAt(LocalDateTime.now());
        cacheById.put(review.getId(), review);
        persistAtomic(toArrayNode());

        return true;
    }

    @Override
    public boolean delete(UUID id) {
        ensureCacheLoaded();
        Review removed = cacheById.remove(id);
        if (removed == null) return false;
        persistAtomic(toArrayNode());
        ReviewServiceInter svc = ensureService();
        if (svc != null) {
            try { svc.delete(id); } catch (RemoteException e) {
                this.service = null;
                throw new ServiceUnavailableException("Server non disponibile", e);
            }
        }
        return true;
    }
}
