package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.server.data.dao.LocationDAO;
import it.uninsubria.laboratoriob.server.data.dao.OwnerDAO;
import it.uninsubria.laboratoriob.server.data.dao.CustomerDAO;
import it.uninsubria.laboratoriob.server.testsupport.DbCleanup;
import it.uninsubria.laboratoriob.server.utils.Database;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests against the real docker-composed Postgres (localhost:5432/mydb).
 * Requires {@code docker compose up -d} to be running.
 *
 * <p>Note: {@link ReviewRepository#count()} hits the DB directly (dao.count() over the
 * whole {@code review} table), while {@code findAll}/{@code findByRestaurant}/{@code findByUser}
 * read purely from the in-memory {@link Restaurant#getReviews()} map via the shared
 * {@link RestaurantRepository}. Count assertions below use before/after deltas since the
 * table is shared with any other process pointed at this Postgres instance.
 */
@DisplayName("ReviewRepository (real Postgres)")
class ReviewRepositoryTest {

    private static final LocationDAO LOCATION_DAO = new LocationDAO();
    private static final OwnerDAO OWNER_DAO = new OwnerDAO();
    private static final CustomerDAO CUSTOMER_DAO = new CustomerDAO();

    private RestaurantRepository restaurantRepo;
    private ReviewRepository reviewRepo;
    private Location location;
    private Owner owner;
    private Customer customer;
    private Restaurant restaurant;
    private Review review;

    @BeforeAll
    static void initSchema() {
        assertTrue(Database.initTables(), "schema init failed - is `docker compose up -d` running?");
        assertTrue(Database.initialiseConstants(), "constants seed failed");
    }

    @BeforeEach
    void setUp() {
        restaurantRepo = new RestaurantRepository();
        reviewRepo = new ReviewRepository(restaurantRepo);

        location = randomLocation();
        assertTrue(LOCATION_DAO.save(location), "test setup: location must persist");

        owner = new Owner(UUID.randomUUID(), uniqueName("owner"), "hash", "salt",
                "Test", "Owner", location, LocalDate.of(1980, 1, 1));
        assertTrue(OWNER_DAO.save(owner), "test setup: owner must persist");

        customer = new Customer(UUID.randomUUID(), uniqueName("customer"), "hash", "salt",
                "Test", "Customer", location, LocalDate.of(1995, 6, 1));
        assertTrue(CUSTOMER_DAO.save(customer), "test setup: customer must persist");

        restaurant = new Restaurant(UUID.randomUUID(), uniqueName("Restaurant"), "desc",
                "https://example.com", owner, "+39 000", location,
                PriceRange.MODERATE, false, false, Award.NONE, false,
                Set.of(CuisineType.ITALIAN), Set.of());
        assertTrue(restaurantRepo.save(restaurant), "test setup: restaurant must persist");

        review = new Review(restaurant, customer, 4, LocalDateTime.now(), "Pretty good", null);
    }

    @AfterEach
    void tearDown() {
        DbCleanup.deleteReview(review.getId());
        DbCleanup.deleteRestaurant(restaurant.getId());
        DbCleanup.deleteUser(customer.getId());
        DbCleanup.deleteUser(owner.getId());
        DbCleanup.deleteLocation(location.getLatitude(), location.getLongitude());
    }

    @Test
    @DisplayName("save() persists to DB and attaches the review to the cached Restaurant")
    void testSave() {
        long before = reviewRepo.count();

        assertTrue(reviewRepo.save(review));

        assertEquals(before + 1, reviewRepo.count());
        assertTrue(restaurant.getReviews().containsKey(review.getId()));
    }

    @Test
    @DisplayName("findByRestaurant() returns reviews attached to the cached Restaurant")
    void testFindByRestaurant() {
        reviewRepo.save(review);

        List<Review> found = reviewRepo.findByRestaurant(restaurant.getId());
        assertEquals(1, found.size());
        assertEquals(review.getId(), found.get(0).getId());

        assertTrue(reviewRepo.findByRestaurant(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("findByUser() filters across all cached restaurants by user id")
    void testFindByUser() {
        reviewRepo.save(review);

        List<Review> found = reviewRepo.findByUser(customer.getId());
        assertEquals(1, found.size());
        assertEquals(review.getId(), found.get(0).getId());

        assertTrue(reviewRepo.findByUser(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("findAll() flattens reviews across all cached restaurants")
    void testFindAll() {
        reviewRepo.save(review);

        assertTrue(reviewRepo.findAll().stream().anyMatch(r -> r.getId().equals(review.getId())));
    }

    @Test
    @DisplayName("delete() removes from DB and from the restaurant's in-memory cache")
    void deleteReview_shouldAlsoRemoveFromRestaurantCache() {
        reviewRepo.save(review);
        long afterSave = reviewRepo.count();

        assertTrue(reviewRepo.delete(review.getId()));
        assertEquals(afterSave - 1, reviewRepo.count(), "DB row should be gone");

        assertTrue(reviewRepo.findByRestaurant(restaurant.getId()).isEmpty());
    }

    @Test
    @DisplayName("update() persists to DB and syncs the restaurant's in-memory cache")
    void updateReview_withNewInstance_shouldSyncRestaurantCache() {
        reviewRepo.save(review);

        Review updated = new Review(review.getId(), restaurant, customer, 1, review.getTimestamp(),
                "Actually terrible", null);
        assertTrue(reviewRepo.update(updated));

        List<Review> cached = reviewRepo.findByRestaurant(restaurant.getId());
        assertEquals(1, cached.size());
        assertEquals(1, cached.get(0).getRating());
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
