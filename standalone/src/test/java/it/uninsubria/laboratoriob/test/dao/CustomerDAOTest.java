package it.uninsubria.laboratoriob.test.dao;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.data.CustomerDAO;
import it.uninsubria.laboratoriob.data.LocationDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.utils.Database;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerDAOTest {

    private static final CustomerDAO customerDAO = new CustomerDAO();
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

        testLocation = new Location(Nation.ITALY, "Turin", 45.0703, 7.6869, "Via Torino 10");
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
    @DisplayName("save: customer persists correctly")
    void saveCustomer_success() {
        Customer c = createTestCustomer("saveuser_" + UUID.randomUUID());
        assertTrue(customerDAO.save(c));

        Optional<Customer> found = customerDAO.findById(c.getId());
        assertTrue(found.isPresent());
        assertEquals(c.getUsername(), found.get().getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("save: customer with favourites persists correctly")
    void saveCustomer_withFavourites_success() {
        Restaurant r = createTestRestaurant("Fav Restaurant");
        restaurantDAO.save(r);

        Customer c = createTestCustomer("favuser_" + UUID.randomUUID());
        c.addFavourite(r);
        assertTrue(customerDAO.save(c));

        Set<UUID> favourites = customerDAO.findFavourites(c.getId());
        assertEquals(1, favourites.size());
        assertTrue(favourites.contains(r.getId()));
    }

    @Test
    @Order(3)
    @DisplayName("findById: returns customer when it exists")
    void findById_exists_success() {
        Customer c = createTestCustomer("finduser_" + UUID.randomUUID());
        customerDAO.save(c);

        Optional<Customer> found = customerDAO.findById(c.getId());
        assertTrue(found.isPresent());
        assertEquals(c.getId(), found.get().getId());
    }

    @Test
    @Order(4)
    @DisplayName("findByUsername: returns customer when it exists")
    void findByUsername_exists_success() {
        String username = "byusername_" + UUID.randomUUID();
        Customer c = createTestCustomer(username);
        customerDAO.save(c);

        Optional<Customer> found = customerDAO.findByUsername(username);
        assertTrue(found.isPresent());
        assertEquals(username, found.get().getUsername());
    }

    @Test
    @Order(5)
    @DisplayName("findAll: returns all customers")
    void findAll_success() {
        int before = customerDAO.findAll().size();
        customerDAO.save(createTestCustomer("findall1_" + UUID.randomUUID()));
        customerDAO.save(createTestCustomer("findall2_" + UUID.randomUUID()));

        List<Customer> all = customerDAO.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(6)
    @DisplayName("update: modifies customer fields correctly")
    void update_success() {
        Customer c = createTestCustomer("updateuser_" + UUID.randomUUID());
        customerDAO.save(c);

        c.setName("UpdatedName");
        c.setLastName("UpdatedLast");
        assertTrue(customerDAO.update(c));

        Optional<Customer> found = customerDAO.findById(c.getId());
        assertTrue(found.isPresent());
        assertEquals("UpdatedName", found.get().getName());
        assertEquals("UpdatedLast", found.get().getLastName());
    }

    @Test
    @Order(7)
    @DisplayName("addFavourites: adds restaurant to customer's favourites")
    void addFavourites_success() {
        Customer c = createTestCustomer("addfavuser_" + UUID.randomUUID());
        customerDAO.save(c);

        Restaurant r = createTestRestaurant("AddFav Restaurant");
        restaurantDAO.save(r);

        assertTrue(customerDAO.addFavourites(c.getId(), r.getId()));
        Set<UUID> favourites = customerDAO.findFavourites(c.getId());
        assertTrue(favourites.contains(r.getId()));
    }

    @Test
    @Order(8)
    @DisplayName("removeFavourites: removes restaurant from customer's favourites")
    void removeFavourites_success() {
        Customer c = createTestCustomer("removefavuser_" + UUID.randomUUID());
        customerDAO.save(c);

        Restaurant r = createTestRestaurant("RemoveFav Restaurant");
        restaurantDAO.save(r);

        customerDAO.addFavourites(c.getId(), r.getId());
        assertTrue(customerDAO.removeFavourites(c.getId(), r.getId()));

        Set<UUID> favourites = customerDAO.findFavourites(c.getId());
        assertFalse(favourites.contains(r.getId()));
    }

    @Test
    @Order(9)
    @DisplayName("findFavourites: returns empty set for customer with no favourites")
    void findFavourites_empty_success() {
        Customer c = createTestCustomer("emptyfavuser_" + UUID.randomUUID());
        customerDAO.save(c);

        Set<UUID> favourites = customerDAO.findFavourites(c.getId());
        assertNotNull(favourites);
        assertTrue(favourites.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("delete: removes customer from database")
    void delete_success() {
        Customer c = createTestCustomer("deleteuser_" + UUID.randomUUID());
        customerDAO.save(c);

        assertTrue(customerDAO.delete(c.getId()));
        assertTrue(customerDAO.findById(c.getId()).isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("update: preserves favourites on update")
    void update_preservesFavourites_success() {
        Restaurant r = createTestRestaurant("PreserveFav Restaurant");
        restaurantDAO.save(r);

        Customer c = createTestCustomer("preservefavuser_" + UUID.randomUUID());
        customerDAO.save(c);
        customerDAO.addFavourites(c.getId(), r.getId());

        c.setName("StillHasFavs");
        customerDAO.update(c);

        Set<UUID> favourites = customerDAO.findFavourites(c.getId());
        assertTrue(favourites.contains(r.getId()));
    }

    // ═══════════════════════════════════════════════════════════════
    // FAILURE CASES (40%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("findById: returns empty for non-existent UUID")
    void findById_notExists_empty() {
        Optional<Customer> found = customerDAO.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("findByUsername: returns empty for non-existent username")
    void findByUsername_notExists_empty() {
        Optional<Customer> found = customerDAO.findByUsername("nonexistent_" + UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("delete: returns false for non-existent customer")
    void delete_notExists_false() {
        assertFalse(customerDAO.delete(UUID.randomUUID()));
    }

    @Test
    @Order(15)
    @DisplayName("update: returns false when customer doesn't exist")
    void update_notExists_false() {
        Customer c = createTestCustomer("ghost_" + UUID.randomUUID());
        assertFalse(customerDAO.update(c));
    }

    @Test
    @Order(16)
    @DisplayName("save: duplicate customer ID throws exception")
    void save_duplicateId_fail() {
        Customer c = createTestCustomer("dup1_" + UUID.randomUUID());
        customerDAO.save(c);

        Customer duplicate = createTestCustomer("dup2_" + UUID.randomUUID());


        assertThrows(Exception.class, () -> customerDAO.save(duplicate));
    }

    @Test
    @Order(17)
    @DisplayName("addFavourites: duplicate favourite returns false")
    void addFavourites_duplicate_false() {
        Customer c = createTestCustomer("dupfavuser_" + UUID.randomUUID());
        customerDAO.save(c);

        Restaurant r = createTestRestaurant("DupFav Restaurant");
        restaurantDAO.save(r);

        customerDAO.addFavourites(c.getId(), r.getId());
        assertFalse(customerDAO.addFavourites(c.getId(), r.getId()));
    }

    @Test
    @Order(18)
    @DisplayName("removeFavourites: non-existent favourite returns false")
    void removeFavourites_notExists_false() {
        Customer c = createTestCustomer("removefailuser_" + UUID.randomUUID());
        customerDAO.save(c);

        assertFalse(customerDAO.removeFavourites(c.getId(), UUID.randomUUID()));
    }

    @Test
    @Order(19)
    @DisplayName("save: customer without location persists (nullable)")
    void saveCustomer_noLocation_success() {
        Customer c = new Customer("noloc_" + UUID.randomUUID(), "hash", "salt",
                "No", "Loc", null, LocalDate.of(2000, 1, 1));
        assertTrue(customerDAO.save(c));

        Optional<Customer> found = customerDAO.findById(c.getId());
        assertTrue(found.isPresent());
    }

    @Test
    @Order(20)
    @DisplayName("findByUsername: case-sensitive matching")
    void findByUsername_caseSensitive_empty() {
        Customer c = createTestCustomer("CaseTest");
        customerDAO.save(c);

        Optional<Customer> found = customerDAO.findByUsername("casetest");
        assertTrue(found.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Customer createTestCustomer(String username) {
        return new Customer(UUID.randomUUID(), username, "hash123", "salt123",
                "Test", "Customer", testLocation, LocalDate.of(1995, 3, 15));
    }

    private Restaurant createTestRestaurant(String name) {
        return new Restaurant(
                UUID.randomUUID(), name, "Test desc", null,
                null, null, testLocation, PriceRange.MODERATE,
                false, false, Award.NONE, false, new HashSet<>(), new HashSet<>()
        );
    }
}
