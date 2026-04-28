package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.enums.Nation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Location Tests")
class LocationTest {

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location(
                Nation.ITALY,
                "Rome",
                41.9028,
                12.4964,
                "Via del Corso, 100"
        );
    }

    @Test
    @DisplayName("Should create location with valid data")
    void testLocationCreation() {
        assertNotNull(location);
        assertEquals(Nation.ITALY, location.getNation());
        assertEquals("Rome", location.getCity());
        assertEquals(41.9028, location.getLatitude());
        assertEquals(12.4964, location.getLongitude());
        assertEquals("Via del Corso, 100", location.getAddress());
    }

    @Test
    @DisplayName("Should build JSON representation")
    void testBuildJsonObject() {
        assertNotNull(location.getJsonObject());
        assertEquals("Italy", location.getJsonObject().get("nation").asText());
        assertEquals("Rome", location.getJsonObject().get("city").asText());
        assertEquals(41.9028, location.getJsonObject().get("latitude").asDouble());
    }

    @Test
    @DisplayName("Should format toString correctly")
    void testToString() {
        String result = location.toString();
        assertTrue(result.contains("Via del Corso, 100"));
        assertTrue(result.contains("Rome"));
        assertTrue(result.contains("ITALY"));
    }

    @Test
    @DisplayName("Should save successfully")
    void testSave() {
        assertTrue(location.save());
    }
}

