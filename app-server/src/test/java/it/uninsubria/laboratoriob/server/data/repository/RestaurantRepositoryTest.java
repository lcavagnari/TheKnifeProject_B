package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.server.data.dao.LocationDAO;
import it.uninsubria.laboratoriob.server.data.dao.OwnerDAO;
import it.uninsubria.laboratoriob.server.data.dao.RestaurantDAO;
import it.uninsubria.laboratoriob.server.testsupport.DbCleanup;
import it.uninsubria.laboratoriob.server.utils.Database;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests against the real docker-composed Postgres (localhost:5432/mydb).
 * Requires {@code docker compose up -d} to be running.
 */
@DisplayName("RestaurantRepository (real Postgres)")
class RestaurantRepositoryTest {

    private static final LocationDAO LOCATION_DAO = new LocationDAO();
    private static final OwnerDAO OWNER_DAO = new OwnerDAO();

    private RestaurantRepository repo;
    private Location location;
    private Owner owner;
    private Restaurant restaurant;

    @BeforeAll
    static void initSchema() {
        assertTrue(Database.initTables(), "schema init failed - is `docker compose up -d` running?");
        assertTrue(Database.initialiseConstants(), "constants seed failed");
    }

    @BeforeEach
    void setUp() {
        repo = new RestaurantRepository();

        location = randomLocation();
        // Workaround for the location-FK bug in RestaurantDAO.save() (see
        // saveRestaurant_withBrandNewLocation_shouldPersist below): pre-save the
        // location so the other tests aren't all blocked by that one bug.
        assertTrue(LOCATION_DAO.save(location), "test setup: location must persist");

        owner = new Owner(UUID.randomUUID(), uniqueName("owner"), "hash", "salt",
                "Test", "Owner", location, LocalDate.of(1980, 1, 1));
        assertTrue(OWNER_DAO.save(owner), "test setup: owner must persist");

        restaurant = new Restaurant(UUID.randomUUID(), uniqueName("Restaurant"), "A place",
                "https://example.com", owner, "+39 000 0000", location,
                PriceRange.EXPENSIVE, true, false, Award.ONE_STAR, false,
                Set.of(CuisineType.ITALIAN), Set.of("WiFi"));
    }

    @AfterEach
    void tearDown() {
        DbCleanup.deleteRestaurant(restaurant.getId());
        DbCleanup.deleteUser(owner.getId());
        DbCleanup.deleteLocation(location.getLatitude(), location.getLongitude());
    }

    @Test
    @DisplayName("save() persists to DB and populates cache")
    void testSaveAndFindById() {
        assertTrue(repo.save(restaurant));

        assertSame(restaurant, repo.findById(restaurant.getId()));
        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("save() of the same id twice fails the second time (PK conflict)")
    void testSaveDuplicateFails() {
        assertTrue(repo.save(restaurant));
        assertFalse(repo.save(restaurant));
    }

    @Test
    @DisplayName("update() overwrites DB row and cache entry")
    void testUpdate() {
        repo.save(restaurant);

        restaurant.setDescription("Updated description");
        restaurant.setName(restaurant.getName() + " Updated");
        assertTrue(repo.update(restaurant));

        assertEquals("Updated description", repo.findById(restaurant.getId()).getDescription());
        assertEquals(restaurant.getName(), repo.findByName(restaurant.getName()).getName());
    }

    @Test
    @DisplayName("delete() removes from cache and DB")
    void testDelete() {
        repo.save(restaurant);

        assertTrue(repo.delete(restaurant.getId()));
        assertNull(repo.findById(restaurant.getId()));
        assertFalse(repo.hasByName(restaurant.getName()));

        // deleted from DB too - a fresh DAO read shouldn't find it either
        assertTrue(new RestaurantDAO().findById(restaurant.getId()).isEmpty());
    }

    @Test
    @DisplayName("findByOwner() filters by owner id")
    void testFindByOwner() {
        repo.save(restaurant);

        List<Restaurant> owned = repo.findByOwner(owner.getId());
        assertEquals(1, owned.size());
        assertEquals(restaurant.getId(), owned.get(0).getId());

        assertTrue(repo.findByOwner(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("updateCuisines() persists to DB and updates the in-memory Restaurant's set")
    void testUpdateCuisinesUpdatesCacheAndDb() {
        repo.save(restaurant);

        Set<CuisineType> newCuisines = Set.of(CuisineType.JAPANESE, CuisineType.SUSHI);
        assertTrue(repo.updateCuisines(restaurant.getId(), newCuisines));

        // in-memory object mutated directly (cache-drift fix from the 2026-08-27 refactor)
        assertEquals(newCuisines, restaurant.getCuisinesTypes());

        // and actually round-trips from the DB, independent of the cache
        assertEquals(newCuisines, repo.findCuisines(restaurant.getId()));
    }

    @Test
    @DisplayName("updateServices() persists to DB and updates the in-memory Restaurant's set")
    void testUpdateServicesUpdatesCacheAndDb() {
        repo.save(restaurant);

        Set<String> newServices = Set.of("Parking", "Outdoor seating");
        assertTrue(repo.updateServices(restaurant.getId(), newServices));

        assertEquals(newServices, restaurant.getServices());
        assertEquals(newServices, repo.findServices(restaurant.getId()));
    }

    @Test
    @DisplayName("save() of a restaurant whose Location was never persisted should still work")
    void saveRestaurant_withBrandNewLocation_shouldPersist() {
        Location freshLocation = randomLocation(); // deliberately NOT pre-saved via LocationDAO
        Restaurant r = new Restaurant(UUID.randomUUID(), uniqueName("FreshLocRestaurant"), "desc",
                "https://example.com", owner, "+39 111", freshLocation,
                PriceRange.MODERATE, false, false, Award.NONE, false, Set.of(), Set.of());

        boolean saved = repo.save(r);

        try {
            assertTrue(saved);
        } finally {
            DbCleanup.deleteRestaurant(r.getId());
            DbCleanup.deleteLocation(freshLocation.getLatitude(), freshLocation.getLongitude());
        }
    }

    @Test
    @DisplayName("PriceRange must round-trip through the DB, not collapse to MODERATE")
    void priceRange_shouldRoundTripThroughDb() {
        restaurant.setPriceRange(PriceRange.LUXURY);
        repo.save(restaurant);

        // bypass the cache entirely - read straight from the DB like a fresh server start would
        Restaurant reloaded = new RestaurantDAO()
                .findById(restaurant.getId())
                .orElseThrow();

        assertEquals(PriceRange.LUXURY, reloaded.getPriceRange());
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
