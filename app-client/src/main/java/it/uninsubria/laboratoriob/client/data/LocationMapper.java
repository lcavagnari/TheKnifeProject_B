package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;

/**
 * Stateless (de)serialization helpers for the embedded {@link Location} value type.
 * Location has no identity of its own (keyed only by coordinates) and is never
 * persisted as a standalone entity - it always lives inline inside the JSON of
 * whatever object references it (restaurant, user, ...).
 */
final class LocationMapper {

    private LocationMapper() {}

    static Location fromNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        return new Location(
                Nation.fromString(node.path("nation").asText()),
                node.path("city").asText(),
                node.path("latitude").asDouble(),
                node.path("longitude").asDouble(),
                node.path("address").asText()
        );
    }

    static ObjectNode toNode(ObjectMapper mapper, Location location) {
        ObjectNode node = mapper.createObjectNode();
        node.put("nation", location.getNation() != null ? location.getNation().name() : "");
        node.put("city", location.getCity());
        node.put("latitude", location.getLatitude());
        node.put("longitude", location.getLongitude());
        node.put("address", location.getAddress());
        return node;
    }
}
