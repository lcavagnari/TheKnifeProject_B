package it.uninsubria.laboratoriob.test.dao;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.data.CustomerDAO;
import it.uninsubria.laboratoriob.data.LocationDAO;
import it.uninsubria.laboratoriob.data.OwnerDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.utils.Database;
import it.uninsubria.laboratoriob.api.enums.Nation;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestaurantDAOTest {

    private static final RestaurantDAO dao = new RestaurantDAO();
    private static final OwnerDAO ownerDAO = new OwnerDAO();
    private static final LocationDAO locationDAO = new LocationDAO();

    private static Owner testOwner;
    private static Location testLocation;

    @BeforeAll
    static void setUp() {
        Database.initTables();
        Database.initialiseConstants();

        testLocation = new Location(Nation.ITALY, "Milan", 45.4642, 9.1900, "Via Roma 1");
        locationDAO.save(testLocation);

        testOwner = new Owner(UUID.randomUUID(), "testowner", "hash123", "salt123",
                "Mario", "Rossi", testLocation, LocalDate.of(1990, 1, 1));
        ownerDAO.save(testOwner);
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
    @DisplayName("save: restaurant with all fields persists correctly")
    void saveRestaurant_success() {
        Restaurant r = createTestRestaurant("Ristorante Uno");
        assertTrue(dao.save(r));

        Optional<Restaurant> found = dao.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Ristorante Uno", found.get().getName());
    }

    @Test
    @Order(2)
    @DisplayName("save: restaurant without location persists correctly")
    void saveRestaurant_noLocation_success() {
        Restaurant r = createTestRestaurant("Ristorante NoLoc");
        r.setLocation(null);
        assertTrue(dao.save(r));

        Optional<Restaurant> found = dao.findById(r.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getLocation());
    }

    @Test
    @Order(3)
    @DisplayName("save: restaurant with all cuisine types persists correctly")
    void saveRestaurant_withCuisines_success() {
        Restaurant r = createTestRestaurant("Ristorante Cucina");
        r.getCuisinesTypes().add(CuisineType.ITALIAN);
        r.getCuisinesTypes().add(CuisineType.JAPANESE);
        assertTrue(dao.save(r));
        dao.updateCuisines(r.getId(), r.getCuisinesTypes());

        Set<CuisineType> cuisines = dao.findCuisines(r.getId());
        assertEquals(2, cuisines.size());
        assertTrue(cuisines.contains(CuisineType.ITALIAN));
        assertTrue(cuisines.contains(CuisineType.JAPANESE));
    }

    @Test
    @Order(4)
    @DisplayName("save: restaurant with services persists correctly")
    void saveRestaurant_withServices_success() {
        Restaurant r = createTestRestaurant("Ristorante Servizi");
        r.getServices().add("WiFi");
        r.getServices().add("Parking");
        assertTrue(dao.save(r));
        dao.updateServices(r.getId(), r.getServices());

        Set<String> services = dao.findServices(r.getId());
        assertEquals(2, services.size());
        assertTrue(services.contains("WiFi"));
        assertTrue(services.contains("Parking"));
    }

    @Test
    @Order(5)
    @DisplayName("findById: returns restaurant when it exists")
    void findById_exists_success() {
        Restaurant r = createTestRestaurant("FindById Test");
        dao.save(r);

        Optional<Restaurant> found = dao.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals(r.getId(), found.get().getId());
    }

    @Test
    @Order(6)
    @DisplayName("findAll: returns all restaurants")
    void findAll_success() {
        int before = dao.findAll().size();
        dao.save(createTestRestaurant("FindAll 1"));
        dao.save(createTestRestaurant("FindAll 2"));

        List<Restaurant> all = dao.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(7)
    @DisplayName("findByOwner: returns restaurants owned by the user")
    void findByOwner_success() {
        Restaurant r = createTestRestaurant("Owner's Restaurant");
        r.setOwner(testOwner);
        dao.save(r);

        List<Restaurant> owners = dao.findByOwner(testOwner.getId());
        assertTrue(owners.stream().anyMatch(x -> x.getId().equals(r.getId())));
    }

    @Test
    @Order(8)
    @DisplayName("update: modifies restaurant fields correctly")
    void update_success() {
        Restaurant r = createTestRestaurant("To Update");
        dao.save(r);

        r.setName("Updated Name");
        r.setDescription("New description");
        r.setGreenStar(true);
        assertTrue(dao.update(r));

        Optional<Restaurant> found = dao.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
        assertEquals("New description", found.get().getDescription());
        assertTrue(found.get().isGreenStar());
    }

    @Test
    @Order(9)
    @DisplayName("updateCuisines: replaces cuisine types correctly")
    void updateCuisines_success() {
        Restaurant r = createTestRestaurant("Cuisine Update");
        dao.save(r);

        Set<CuisineType> initial = new HashSet<>(Set.of(CuisineType.FRENCH));
        dao.updateCuisines(r.getId(), initial);
        assertEquals(1, dao.findCuisines(r.getId()).size());

        Set<CuisineType> updated = new HashSet<>(Set.of(CuisineType.ITALIAN, CuisineType.SPANISH));
        dao.updateCuisines(r.getId(), updated);

        Set<CuisineType> result = dao.findCuisines(r.getId());
        assertEquals(2, result.size());
        assertTrue(result.contains(CuisineType.ITALIAN));
        assertTrue(result.contains(CuisineType.SPANISH));
    }

    @Test
    @Order(10)
    @DisplayName("updateServices: replaces services correctly")
    void updateServices_success() {
        Restaurant r = createTestRestaurant("Service Update");
        dao.save(r);

        Set<String> initial = new HashSet<>(Set.of("WiFi"));
        dao.updateServices(r.getId(), initial);
        assertEquals(1, dao.findServices(r.getId()).size());

        Set<String> updated = new HashSet<>(Set.of("Parking", "Delivery"));
        dao.updateServices(r.getId(), updated);

        Set<String> result = dao.findServices(r.getId());
        assertEquals(2, result.size());
        assertTrue(result.contains("Parking"));
        assertTrue(result.contains("Delivery"));
    }

    @Test
    @Order(11)
    @DisplayName("delete: removes restaurant from database")
    void delete_success() {
        Restaurant r = createTestRestaurant("To Delete");
        dao.save(r);

        assertTrue(dao.delete(r.getId()));
        assertTrue(dao.findById(r.getId()).isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("findCuisines: returns empty set for restaurant with no cuisines")
    void findCuisines_empty_success() {
        Restaurant r = createTestRestaurant("No Cuisines");
        dao.save(r);

        Set<CuisineType> cuisines = dao.findCuisines(r.getId());
        assertNotNull(cuisines);
        assertTrue(cuisines.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    // FAILURE CASES (40%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("findById: returns empty for non-existent UUID")
    void findById_notExists_empty() {
        UUID fakeId = UUID.randomUUID();
        Optional<Restaurant> found = dao.findById(fakeId);
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("findByOwner: returns empty for owner with no restaurants")
    void findByOwner_noRestaurants_empty() {
        Owner lonely = new Owner(UUID.randomUUID(), "lonely_" + UUID.randomUUID(), "h", "s",
                "Lonely", "Owner", null, LocalDate.of(2000, 1, 1));
        ownerDAO.save(lonely);

        List<Restaurant> result = dao.findByOwner(lonely.getId());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(15)
    @DisplayName("delete: returns false for non-existent restaurant")
    void delete_notExists_false() {
        assertFalse(dao.delete(UUID.randomUUID()));
    }

    @Test
    @Order(16)
    @DisplayName("update: returns false when restaurant doesn't exist")
    void update_notExists_false() {
        Restaurant r = createTestRestaurant("Ghost Restaurant");
        assertFalse(dao.update(r));
    }

    @Test
    @Order(17)
    @DisplayName("save: duplicate restaurant ID throws or returns false")
    void save_duplicateId_fail() {
        Restaurant r = createTestRestaurant("Duplicate Test");
        dao.save(r);

        Restaurant duplicate = createTestRestaurant("Duplicate Test 2");

        assertThrows(Exception.class, () -> dao.save(duplicate));
    }

    @Test
    @Order(18)
    @DisplayName("updateCuisines: with empty set clears all cuisines")
    void updateCuisines_empty_clears() {
        Restaurant r = createTestRestaurant("Clear Cuisines");
        dao.save(r);
        dao.updateCuisines(r.getId(), Set.of(CuisineType.ITALIAN));

        dao.updateCuisines(r.getId(), Set.of());
        assertTrue(dao.findCuisines(r.getId()).isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("updateServices: with empty set clears all services")
    void updateServices_empty_clears() {
        Restaurant r = createTestRestaurant("Clear Services");
        dao.save(r);
        dao.updateServices(r.getId(), Set.of("WiFi"));

        dao.updateServices(r.getId(), Set.of());
        assertTrue(dao.findServices(r.getId()).isEmpty());
    }

    @Test
    @Order(20)
    @DisplayName("findServices: returns empty set for restaurant with no services")
    void findServices_empty_success() {
        Restaurant r = createTestRestaurant("No Services");
        dao.save(r);

        Set<String> services = dao.findServices(r.getId());
        assertNotNull(services);
        assertTrue(services.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Restaurant createTestRestaurant(String name) {
        return new Restaurant(
                UUID.randomUUID(),
                name,
                "Test description",
                "https://example.com",
                testOwner,
                "+39 02 1234567",
                testLocation,
                PriceRange.MODERATE,
                false,
                false,
                Award.NONE,
                false,
                new HashSet<>(),
                new HashSet<>()
        );
    }
}
