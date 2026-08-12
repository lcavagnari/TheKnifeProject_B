package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementazione JSON del DAO per l'entità {@link Review}.
 * <p>
 * Gestisce le operazioni CRUD sulle recensioni usando file JSON come storage locale.
 * </p>
 */
public final class JsonReviewDAO implements DAO<Review> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final File storeFile;
    private final JsonCustomerDAO customerDAO;

    public JsonReviewDAO(JsonCustomerDAO customerDAO) {
        this.storeFile = new File(Constants.ROOT, "reviews.json");
        this.customerDAO = customerDAO;
    }

    private ArrayNode loadAll() {
        if (!storeFile.exists()) return mapper.createArrayNode();
        try {
            JsonNode node = mapper.readTree(storeFile);
            return node.isArray() ? (ArrayNode) node : mapper.createArrayNode();
        } catch (IOException e) {
            System.err.println("Errore loadAll in JsonReviewDAO: " + e.getMessage());
            return mapper.createArrayNode();
        }
    }

    private void persist(ArrayNode array) {
        try {
            if (!storeFile.getParentFile().exists()) storeFile.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(storeFile, array);
        } catch (IOException e) {
            System.err.println("Errore persist in JsonReviewDAO: " + e.getMessage());
        }
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
        ArrayNode array = loadAll();
        for (JsonNode node : array) {
            if (node.path("id").asText().equals(id.toString())) {
                return Optional.of(mapNode(node));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Review> findAll() {
        ArrayNode array = loadAll();
        List<Review> reviews = new ArrayList<>();
        for (JsonNode node : array) {
            reviews.add(mapNode(node));
        }
        return reviews;
    }

    public List<Review> findByRestaurant(UUID restaurantId) {
        return findAll().stream()
                .filter(r -> r.getRestaurant() != null && r.getRestaurant().getId().equals(restaurantId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean save(Review review) {
        if (review == null) return false;
        ArrayNode array = loadAll();

        for (JsonNode node : array) {
            if (node.path("id").asText().equals(review.getId().toString())) {
                return false;
            }
        }

        array.add(toNode(review));
        persist(array);
        return true;
    }

    @Override
    public boolean update(Review review) {
        if (review == null) return false;
        ArrayNode array = loadAll();

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).path("id").asText().equals(review.getId().toString())) {
                array.set(i, toNode(review));
                persist(array);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        ArrayNode array = loadAll();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).path("id").asText().equals(id.toString())) {
                array.remove(i);
                persist(array);
                return true;
            }
        }
        return false;
    }
}
