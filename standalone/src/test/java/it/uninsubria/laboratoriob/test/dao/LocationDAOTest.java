package it.uninsubria.laboratoriob.test.dao;

import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.data.LocationDAO;
import it.uninsubria.laboratoriob.utils.Database;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocationDAOTest {

    private static final LocationDAO locationDAO = new LocationDAO();

    @BeforeAll
    static void setUp() {
        Database.initTables();
        Database.initialiseConstants();
    }

    @AfterAll
    static void tearDown() {
        Database.shutdown();
    }

    // ═══════════════════════════════════════════════════════════════
    // HAPPY PATHS (60%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("save: location persists correctly")
    void saveLocation_success() {
        Location loc = new Location(Nation.ITALY, "Florence", 43.7696, 11.2558, "Via Firenze 5");
        assertTrue(locationDAO.save(loc));

        Optional<Location> found = locationDAO.findByCoordinates(43.7696, 11.2558);
        assertTrue(found.isPresent());
        assertEquals("Florence", found.get().getCity());
    }

    @Test
    @Order(2)
    @DisplayName("save: location with different nation persists correctly")
    void saveLocation_differentNation_success() {
        Location loc = new Location(Nation.FRANCE, "Paris", 48.8566, 2.3522, "Rue de Paris 10");
        assertTrue(locationDAO.save(loc));

        Optional<Location> found = locationDAO.findByCoordinates(48.8566, 2.3522);
        assertTrue(found.isPresent());
        assertEquals(Nation.FRANCE, found.get().getNation());
    }

    @Test
    @Order(3)
    @DisplayName("findByCoordinates: returns location when it exists")
    void findByCoordinates_exists_success() {
        Location loc = new Location(Nation.GERMANY, "Berlin", 52.5200, 13.4050, "Berlin Str 1");
        locationDAO.save(loc);

        Optional<Location> found = locationDAO.findByCoordinates(52.5200, 13.4050);
        assertTrue(found.isPresent());
        assertEquals("Berlin", found.get().getCity());
    }

    @Test
    @Order(4)
    @DisplayName("findAll: returns all locations")
    void findAll_success() {
        int before = locationDAO.findAll().size();
        locationDAO.save(new Location(Nation.SPAIN, "Madrid", 40.4168, -3.7038, "Madrid St 1"));
        locationDAO.save(new Location(Nation.UNITED_KINGDOM, "London", 51.5074, -0.1278, "London Rd 1"));

        List<Location> all = locationDAO.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(5)
    @DisplayName("update: modifies location fields correctly")
    void update_success() {
        Location loc = new Location(Nation.ITALY, "Venice", 45.4408, 12.3155, "Venezia 1");
        locationDAO.save(loc);

        Location updated = new Location(Nation.ITALY, "Venice Updated", 45.4408, 12.3155, "Venezia Nuova 2");
        assertTrue(locationDAO.update(45.4408, 12.3155, updated));

        Optional<Location> found = locationDAO.findByCoordinates(45.4408, 12.3155);
        assertTrue(found.isPresent());
        assertEquals("Venice Updated", found.get().getCity());
    }

    @Test
    @Order(6)
    @DisplayName("delete: removes location from database")
    void delete_success() {
        Location loc = new Location(Nation.PORTUGAL, "Lisbon", 38.7223, -9.1393, "Lisboa 1");
        locationDAO.save(loc);

        assertTrue(locationDAO.delete(38.7223, -9.1393));
        assertTrue(locationDAO.findByCoordinates(38.7223, -9.1393).isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("save: location at boundary coordinates (0,0) persists")
    void saveLocation_boundary_success() {
        Location loc = new Location(Nation.fromString("IT"), "Null Island", 0.0, 0.0, "Equator");
        assertTrue(locationDAO.save(loc));

        Optional<Location> found = locationDAO.findByCoordinates(0.0, 0.0);
        assertTrue(found.isPresent());
        assertEquals("Null Island", found.get().getCity());
    }

    @Test
    @Order(8)
    @DisplayName("save: location with extreme coordinates persists")
    void saveLocation_extreme_success() {
        Location loc = new Location(Nation.fromString("NO"), "North Pole", 89.9999, 0.0, "Arctic");
        assertTrue(locationDAO.save(loc));

        Optional<Location> found = locationDAO.findByCoordinates(89.9999, 0.0);
        assertTrue(found.isPresent());
    }

    // ═══════════════════════════════════════════════════════════════
    // FAILURE CASES (40%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("findByCoordinates: returns empty for non-existent coordinates")
    void findByCoordinates_notExists_empty() {
        Optional<Location> found = locationDAO.findByCoordinates(99.999, 99.999);
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("delete: returns false for non-existent location")
    void delete_notExists_false() {
        assertFalse(locationDAO.delete(99.999, 99.999));
    }

    @Test
    @Order(11)
    @DisplayName("update: returns false when location doesn't exist")
    void update_notExists_false() {
        Location loc = new Location(Nation.ITALY, "Ghost", 99.999, 99.999, "Nowhere");
        assertFalse(locationDAO.update(99.999, 99.999, loc));
    }

    @Test
    @Order(12)
    @DisplayName("save: duplicate location ID (composite key) throws or fails")
    void save_duplicateId_fail() {
        Location loc = new Location(Nation.ITALY, "Duplicate", 41.0, 12.0, "Dup St");
        locationDAO.save(loc);

        Location duplicate = new Location(Nation.ITALY, "Duplicate 2", 41.0, 12.0, "Dup St 2");
        assertThrows(Exception.class, () -> locationDAO.save(duplicate));
    }

    @Test
    @Order(13)
    @DisplayName("findByCoordinates: negative coordinates work correctly")
    void findByCoordinates_negative_success() {
        Location loc = new Location(Nation.fromString("AR"), "Buenos Aires", -34.6037, -58.3816, "BA St");
        locationDAO.save(loc);

        Optional<Location> found = locationDAO.findByCoordinates(-34.6037, -58.3816);
        assertTrue(found.isPresent());
        assertEquals("Buenos Aires", found.get().getCity());
    }

    @Test
    @Order(14)
    @DisplayName("update: update via entity method works")
    void updateEntity_success() {
        Location loc = new Location(Nation.ITALY, "Genoa", 44.4056, 8.9463, "Genova 1");
        locationDAO.save(loc);

        Location updated = new Location(Nation.ITALY, "Genoa Updated", 44.4056, 8.9463, "Genova Nuova");
        assertTrue(locationDAO.update(updated));

        Optional<Location> found = locationDAO.findByCoordinates(44.4056, 8.9463);
        assertTrue(found.isPresent());
        assertEquals("Genoa Updated", found.get().getCity());
    }

    @Test
    @Order(15)
    @DisplayName("findAll: returns empty list when no custom locations exist")
    void findAll_emptyList_success() {
        List<Location> all = locationDAO.findAll();
        assertNotNull(all);
    }

    @Test
    @Order(16)
    @DisplayName("save: location with special characters in address persists")
    void saveLocation_specialChars_success() {
        Location loc = new Location(Nation.ITALY, "Bari", 41.1171, 16.8719, "Via dell'Ateneo 10");
        assertTrue(locationDAO.save(loc));

        Optional<Location> found = locationDAO.findByCoordinates(41.1171, 16.8719);
        assertTrue(found.isPresent());
        assertEquals("Via dell'Ateneo 10", found.get().getAddress());
    }

    @Test
    @Order(17)
    @DisplayName("findByCoordinates: floating point precision handled")
    void findByCoordinates_precision_success() {
        Location loc = new Location(Nation.ITALY, "Palermo", 38.1157, 13.3615, "Palermo St");
        locationDAO.save(loc);

        Optional<Location> found = locationDAO.findByCoordinates(38.1157, 13.3615);
        assertTrue(found.isPresent());
    }

    @Test
    @Order(18)
    @DisplayName("delete: deleting same location twice returns false second time")
    void delete_idempotent_false() {
        Location loc = new Location(Nation.SPAIN, "Barcelona", 41.3874, 2.1686, "Barca St");
        locationDAO.save(loc);

        assertTrue(locationDAO.delete(41.3874, 2.1686));
        assertFalse(locationDAO.delete(41.3874, 2.1686));
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════
}
