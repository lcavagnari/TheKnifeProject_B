package it.uninsubria.laboratoriob.server.data;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.server.data.dao.LocationDAO;
import it.uninsubria.laboratoriob.server.testsupport.DbCleanup;
import it.uninsubria.laboratoriob.server.utils.Database;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests against the real docker-composed Postgres (localhost:5432/mydb).
 * Requires {@code docker compose up -d} to be running.
 */
@DisplayName("ServerDataStore (real Postgres)")
class ServerDataStoreTest {

    private static final LocationDAO LOCATION_DAO = new LocationDAO();

    private ServerDataStore store;
    private Location location;
    private Owner owner;
    private Customer customer;
    private Restaurant restaurant;

    @BeforeAll
    static void initSchema() {
        assertTrue(Database.initTables(), "schema init failed - is `docker compose up -d` running?");
        assertTrue(Database.initialiseConstants(), "constants seed failed");
    }

    @BeforeEach
    void setUp() {
        store = new ServerDataStore();

        location = randomLocation();
        assertTrue(LOCATION_DAO.save(location), "test setup: location must persist");

        owner = new Owner(UUID.randomUUID(), uniqueName("owner"), "hash", "salt",
                "Test", "Owner", location, LocalDate.of(1980, 1, 1));
        customer = new Customer(UUID.randomUUID(), uniqueName("customer"), "hash", "salt",
                "Test", "Customer", location, LocalDate.of(1995, 6, 1));

        restaurant = new Restaurant(UUID.randomUUID(), uniqueName("Restaurant"), "desc",
                "https://example.com", owner, "+39 000", location,
                PriceRange.MODERATE, false, false, Award.NONE, false,
                Set.of(CuisineType.ITALIAN), Set.of());
    }

    @AfterEach
    void tearDown() {
        DbCleanup.deleteRestaurant(restaurant.getId());
        DbCleanup.deleteUser(owner.getId());
        DbCleanup.deleteUser(customer.getId());
        DbCleanup.deleteLocation(location.getLatitude(), location.getLongitude());
    }

    @Test
    @DisplayName("restaurants()/reviews()/users() return stable singleton accessors")
    void testAccessorsAreStable() {
        assertNotNull(store.restaurants());
        assertNotNull(store.reviews());
        assertNotNull(store.users());

        assertSame(store.restaurants(), store.restaurants());
        assertSame(store.reviews(), store.reviews());
        assertSame(store.users(), store.users());
    }

    @Test
    @DisplayName("reviews() shares the same RestaurantRepository instance as restaurants()")
    void testReviewsShareRestaurantCache() {
        store.users().saveOwner(owner);
        store.users().saveCustomer(customer);
        assertTrue(store.restaurants().save(restaurant));

        Review review = new Review(restaurant, customer, 5, LocalDateTime.now(), "Loved it", null);
        try {
            assertTrue(store.reviews().save(review));

            // save() attached the review to the SAME Restaurant instance cached in restaurants(),
            // proving reviews() and restaurants() are wired to a shared RestaurantRepository -
            // not two independent caches that would silently diverge.
            assertTrue(store.restaurants().findById(restaurant.getId()).getReviews().containsKey(review.getId()));
            assertEquals(1, store.reviews().findByRestaurant(restaurant.getId()).size());
        } finally {
            DbCleanup.deleteReview(review.getId());
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
