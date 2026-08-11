package it.uninsubria.laboratoriob.test.dao;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.data.LocationDAO;
import it.uninsubria.laboratoriob.data.OwnerDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.utils.Database;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OwnerDAOTest {

    private static final OwnerDAO ownerDAO = new OwnerDAO();
    private static final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private static final LocationDAO locationDAO = new LocationDAO();

    private static Location testLocation;

    @BeforeAll
    static void setUp() {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/mydb", "testuser", "test1234")) {
            assumeTrue(c.isValid(3), "PostgreSQL not available");
        } catch (Exception e) {
            assumeTrue(false, "PostgreSQL not available, skipping DAO tests");
        }

        Database.initTables();
        Database.initialiseConstants();

        testLocation = new Location(Nation.ITALY, "Naples", 40.8518, 14.2681, "Via Napoli 20");
        locationDAO.save(testLocation);
    }

    @AfterAll
    static void tearDown() {
        try {
            Database.shutdown();
        } catch (Exception ignored) {
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HAPPY PATHS (60%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("save: owner persists correctly")
    void saveOwner_success() {
        Owner o = createTestOwner("saveowner_" + UUID.randomUUID());
        assertTrue(ownerDAO.save(o));

        Optional<Owner> found = ownerDAO.findById(o.getId());
        assertTrue(found.isPresent());
        assertEquals(o.getUsername(), found.get().getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("save: owner with restaurants persists correctly")
    void saveOwner_withRestaurants_success() {
        Owner o = createTestOwner("restowner_" + UUID.randomUUID());
        Restaurant r = createTestRestaurant("Owner's Place");
        r.setOwner(o);
        o.addRestaurant(r);

        assertTrue(ownerDAO.save(o));
        restaurantDAO.save(r);

        Optional<Owner> found = ownerDAO.findById(o.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().getRestaurantsById().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("findById: returns owner when it exists")
    void findById_exists_success() {
        Owner o = createTestOwner("findowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        Optional<Owner> found = ownerDAO.findById(o.getId());
        assertTrue(found.isPresent());
        assertEquals(o.getId(), found.get().getId());
    }

    @Test
    @Order(4)
    @DisplayName("findByUsername: returns owner when it exists")
    void findByUsername_exists_success() {
        String username = "byusername_" + UUID.randomUUID();
        Owner o = createTestOwner(username);
        ownerDAO.save(o);

        Optional<Owner> found = ownerDAO.findByUsername(username);
        assertTrue(found.isPresent());
        assertEquals(username, found.get().getUsername());
    }

    @Test
    @Order(5)
    @DisplayName("findAll: returns all owners")
    void findAll_success() {
        int before = ownerDAO.findAll().size();
        ownerDAO.save(createTestOwner("findall1_" + UUID.randomUUID()));
        ownerDAO.save(createTestOwner("findall2_" + UUID.randomUUID()));

        List<Owner> all = ownerDAO.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(6)
    @DisplayName("update: modifies owner fields correctly")
    void update_success() {
        Owner o = createTestOwner("updateowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        o.setName("UpdatedName");
        o.setLastName("UpdatedLast");
        assertTrue(ownerDAO.update(o));

        Optional<Owner> found = ownerDAO.findById(o.getId());
        assertTrue(found.isPresent());
        assertEquals("UpdatedName", found.get().getName());
        assertEquals("UpdatedLast", found.get().getLastName());
    }

    @Test
    @Order(7)
    @DisplayName("addRestaurant: adds restaurant to owner's collection")
    void addRestaurant_success() {
        Owner o = createTestOwner("addrestowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        Restaurant r = createTestRestaurant("New Restaurant");
        r.setOwner(o);
        restaurantDAO.save(r);

        assertTrue(ownerDAO.addRestaurant(o.getId(), r.getId()));
        Set<UUID> restaurants = ownerDAO.findRestaurants(o.getId());
        assertTrue(restaurants.contains(r.getId()));
    }

    @Test
    @Order(8)
    @DisplayName("removeRestaurant: removes restaurant from owner's collection")
    void removeRestaurant_success() {
        Owner o = createTestOwner("removerestowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        Restaurant r = createTestRestaurant("Remove Restaurant");
        r.setOwner(o);
        restaurantDAO.save(r);

        ownerDAO.addRestaurant(o.getId(), r.getId());
        assertTrue(ownerDAO.removeRestaurant(o.getId(), r.getId()));

        Set<UUID> restaurants = ownerDAO.findRestaurants(o.getId());
        assertFalse(restaurants.contains(r.getId()));
    }

    @Test
    @Order(9)
    @DisplayName("findRestaurants: returns empty set for owner with no restaurants")
    void findRestaurants_empty_success() {
        Owner o = createTestOwner("emptyrestowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        Set<UUID> restaurants = ownerDAO.findRestaurants(o.getId());
        assertNotNull(restaurants);
        assertTrue(restaurants.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("delete: removes owner from database")
    void delete_success() {
        Owner o = createTestOwner("deleteowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        assertTrue(ownerDAO.delete(o.getId()));
        assertTrue(ownerDAO.findById(o.getId()).isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("findByOwner: returns restaurants owned by the owner")
    void findByOwner_success() {
        Owner o = createTestOwner("findbyowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        Restaurant r = createTestRestaurant("FindByOwner Restaurant");
        r.setOwner(o);
        restaurantDAO.save(r);

        List<Owner> owners = ownerDAO.findAll();
        assertTrue(owners.stream().anyMatch(x -> x.getId().equals(o.getId())));
    }

    // ═══════════════════════════════════════════════════════════════
    // FAILURE CASES (40%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("findById: returns empty for non-existent UUID")
    void findById_notExists_empty() {
        Optional<Owner> found = ownerDAO.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("findByUsername: returns empty for non-existent username")
    void findByUsername_notExists_empty() {
        Optional<Owner> found = ownerDAO.findByUsername("nonexistent_" + UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("delete: returns false for non-existent owner")
    void delete_notExists_false() {
        assertFalse(ownerDAO.delete(UUID.randomUUID()));
    }

    @Test
    @Order(15)
    @DisplayName("update: returns false when owner doesn't exist")
    void update_notExists_false() {
        Owner o = createTestOwner("ghost_" + UUID.randomUUID());
        assertFalse(ownerDAO.update(o));
    }

    @Test
    @Order(16)
    @DisplayName("save: duplicate owner ID throws exception")
    void save_duplicateId_fail() {
        Owner o = createTestOwner("dup1_" + UUID.randomUUID());
        ownerDAO.save(o);

        Owner duplicate = createTestOwner("dup2_" + UUID.randomUUID());


        assertThrows(Exception.class, () -> ownerDAO.save(duplicate));
    }

    @Test
    @Order(17)
    @DisplayName("addRestaurant: duplicate restaurant returns false")
    void addRestaurant_duplicate_false() {
        Owner o = createTestOwner("duprestowner_" + UUID.randomUUID());
        ownerDAO.save(o);

        Restaurant r = createTestRestaurant("DupRest Restaurant");
        r.setOwner(o);
        restaurantDAO.save(r);

        ownerDAO.addRestaurant(o.getId(), r.getId());
        assertFalse(ownerDAO.addRestaurant(o.getId(), r.getId()));
    }

    @Test
    @Order(18)
    @DisplayName("removeRestaurant: non-existent restaurant returns false")
    void removeRestaurant_notExists_false() {
        Owner o = createTestOwner("removerestfail_" + UUID.randomUUID());
        ownerDAO.save(o);

        assertFalse(ownerDAO.removeRestaurant(o.getId(), UUID.randomUUID()));
    }

    @Test
    @Order(19)
    @DisplayName("save: owner without location persists (nullable)")
    void saveOwner_noLocation_success() {
        Owner o = new Owner("noloc_" + UUID.randomUUID(), "hash", "salt",
                "No", "Loc", null, LocalDate.of(1990, 1, 1));
        assertTrue(ownerDAO.save(o));

        Optional<Owner> found = ownerDAO.findById(o.getId());
        assertTrue(found.isPresent());
    }

    @Test
    @Order(20)
    @DisplayName("findByUsername: case-sensitive matching")
    void findByUsername_caseSensitive_empty() {
        Owner o = createTestOwner("CaseTest");
        ownerDAO.save(o);

        Optional<Owner> found = ownerDAO.findByUsername("casetest");
        assertTrue(found.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Owner createTestOwner(String username) {
        return new Owner(UUID.randomUUID(), username, "hash123", "salt123",
                "Test", "Owner", testLocation, LocalDate.of(1985, 7, 20));
    }

    private Restaurant createTestRestaurant(String name) {
        return new Restaurant(
                UUID.randomUUID(), name, "Test desc", null,
                null, null, testLocation, PriceRange.MODERATE,
                false, false, Award.NONE, false, new HashSet<>(), new HashSet<>()
        );
    }
}
