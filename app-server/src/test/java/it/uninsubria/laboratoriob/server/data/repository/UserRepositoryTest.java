package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.server.data.dao.LocationDAO;
import it.uninsubria.laboratoriob.server.data.dao.RestaurantDAO;
import it.uninsubria.laboratoriob.server.testsupport.DbCleanup;
import it.uninsubria.laboratoriob.server.utils.Database;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests against the real docker-composed Postgres (localhost:5432/mydb).
 * Requires {@code docker compose up -d} to be running.
 */
@DisplayName("UserRepository (real Postgres)")
class UserRepositoryTest {

    private static final LocationDAO LOCATION_DAO = new LocationDAO();
    private static final RestaurantDAO RESTAURANT_DAO = new RestaurantDAO();

    private UserRepository repo;
    private Location location;
    private Customer customer;
    private Owner owner;

    @BeforeAll
    static void initSchema() {
        assertTrue(Database.initTables(), "schema init failed - is `docker compose up -d` running?");
        assertTrue(Database.initialiseConstants(), "constants seed failed");
    }

    @BeforeEach
    void setUp() {
        repo = new UserRepository();

        location = randomLocation();
        assertTrue(LOCATION_DAO.save(location), "test setup: location must persist");

        customer = new Customer(UUID.randomUUID(), uniqueName("customer"), "hash", "salt",
                "Test", "Customer", location, LocalDate.of(1995, 6, 1));
        owner = new Owner(UUID.randomUUID(), uniqueName("owner"), "hash", "salt",
                "Test", "Owner", location, LocalDate.of(1980, 1, 1));
    }

    @AfterEach
    void tearDown() {
        DbCleanup.deleteUser(customer.getId());
        DbCleanup.deleteUser(owner.getId());
        DbCleanup.deleteLocation(location.getLatitude(), location.getLongitude());
    }

    @Test
    @DisplayName("saveCustomer() persists to DB and populates cache")
    void testSaveCustomer() {
        assertTrue(repo.saveCustomer(customer));

        assertSame(customer, repo.findById(customer.getId()));
        assertEquals(customer.getId(), repo.findByName(customer.getUsername()).getId());
        assertTrue(repo.hasByName(customer.getUsername()));
        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("saveOwner() persists to DB and populates cache")
    void testSaveOwner() {
        assertTrue(repo.saveOwner(owner));

        assertSame(owner, repo.findById(owner.getId()));
        assertEquals(owner.getId(), repo.findByName(owner.getUsername()).getId());
    }

    @Test
    @DisplayName("updateCustomer() overwrites DB row and cache entry")
    void testUpdateCustomer() {
        repo.saveCustomer(customer);

        Customer updated = new Customer(customer.getId(), customer.getUsername(), "newhash", "newsalt",
                "Test", "Customer", location, LocalDate.of(1995, 6, 1));
        assertTrue(repo.updateCustomer(updated));

        assertEquals("newhash", repo.findById(customer.getId()).getPasswordHash());
    }

    @Test
    @DisplayName("updateOwner() overwrites DB row and cache entry")
    void testUpdateOwner() {
        repo.saveOwner(owner);

        Owner updated = new Owner(owner.getId(), owner.getUsername(), "newhash", "newsalt",
                "Test", "Owner", location, LocalDate.of(1980, 1, 1));
        assertTrue(repo.updateOwner(updated));

        assertEquals("newhash", repo.findById(owner.getId()).getPasswordHash());
    }

    @Test
    @DisplayName("delete() dispatches to the right DAO based on cached type (Customer)")
    void testDeleteCustomer() {
        repo.saveCustomer(customer);

        assertTrue(repo.delete(customer.getId()));
        assertNull(repo.findById(customer.getId()));
        assertFalse(repo.hasByName(customer.getUsername()));
    }

    @Test
    @DisplayName("delete() dispatches to the right DAO based on cached type (Owner)")
    void testDeleteOwner() {
        repo.saveOwner(owner);

        assertTrue(repo.delete(owner.getId()));
        assertNull(repo.findById(owner.getId()));
    }

    @Test
    @DisplayName("delete() of an unknown id returns false")
    void testDeleteUnknownReturnsFalse() {
        assertFalse(repo.delete(UUID.randomUUID()));
    }

    @Nested
    @DisplayName("Favourites (require a real restaurant row)")
    class Favourites {

        private Restaurant restaurant;

        @BeforeEach
        void setUpRestaurant() {
            repo.saveCustomer(customer);
            repo.saveOwner(owner);

            restaurant = new Restaurant(UUID.randomUUID(), uniqueName("Restaurant"), "desc",
                    "https://example.com", owner, "+39 000", location,
                    PriceRange.MODERATE, false, false, Award.NONE, false,
                    Set.of(CuisineType.ITALIAN), Set.of());
            assertTrue(RESTAURANT_DAO.save(restaurant), "test setup: restaurant must persist");
        }

        @AfterEach
        void tearDownRestaurant() {
            DbCleanup.deleteRestaurant(restaurant.getId());
        }

        @Test
        @DisplayName("addFavourite() persists to DB and updates the in-memory Customer")
        void testAddFavourite() {
            assertTrue(repo.addFavourite(customer.getId(), restaurant.getId()));

            assertTrue(customer.getFavouriteRestourants().contains(restaurant.getId()));
            assertEquals(Set.of(restaurant.getId()), repo.findFavourites(customer.getId()));
        }

        @Test
        @DisplayName("removeFavourite() persists to DB and updates the in-memory Customer")
        void testRemoveFavourite() {
            repo.addFavourite(customer.getId(), restaurant.getId());

            assertTrue(repo.removeFavourite(customer.getId(), restaurant.getId()));
            assertFalse(customer.getFavouriteRestourants().contains(restaurant.getId()));
            assertTrue(repo.findFavourites(customer.getId()).isEmpty());
        }
    }

    private static Location randomLocation() {
        double lat = -60 + Math.random() * 120;
        double lon = -170 + Math.random() * 340;
        return new Location(Nation.ITALY, "TestCity", lat, lon, "Via Test " + UUID.randomUUID());
    }

    private static String uniqueName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
