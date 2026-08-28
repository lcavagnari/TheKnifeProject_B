package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("LocationMapper Tests")
class LocationMapperTest {

    private Location testLocation;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        testLocation = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");
    }

    @Test
    @DisplayName("toNode() converts Location to ObjectNode correctly")
    void testToNode() {
        ObjectNode node = LocationMapper.toNode(mapper, testLocation);

        assertEquals("ITALY", node.path("nation").asText());
        assertEquals("Milano", node.path("city").asText());
        assertEquals(45.4642, node.path("latitude").asDouble());
        assertEquals(9.1900, node.path("longitude").asDouble());
        assertEquals("Via Garibaldi 5", node.path("address").asText());
    }

    @Test
    @DisplayName("fromNode() parses ObjectNode to Location correctly")
    void testFromNode() {
        ObjectNode node = mapper.createObjectNode();
        node.put("nation", "FRANCE");
        node.put("city", "Paris");
        node.put("latitude", 48.8566);
        node.put("longitude", 2.3522);
        node.put("address", "Rue Test");

        Location parsed = LocationMapper.fromNode(node);

        assertEquals(Nation.FRANCE, parsed.getNation());
        assertEquals("Paris", parsed.getCity());
        assertEquals(48.8566, parsed.getLatitude());
        assertEquals(2.3522, parsed.getLongitude());
        assertEquals("Rue Test", parsed.getAddress());
    }

    @Test
    @DisplayName("fromNode() returns null for null or missing node")
    void testFromNodeNullHandling() {
        assertNull(LocationMapper.fromNode(null));
        assertNull(LocationMapper.fromNode(mapper.missingNode()));
        assertNull(LocationMapper.fromNode(mapper.nullNode()));
    }
}