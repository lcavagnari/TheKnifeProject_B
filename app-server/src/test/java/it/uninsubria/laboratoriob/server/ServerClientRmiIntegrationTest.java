package it.uninsubria.laboratoriob.server;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.server.testsupport.DbCleanup;
import it.uninsubria.laboratoriob.server.utils.Database;
import org.junit.jupiter.api.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test: starts the real server entry point ({@link TheKnifeServer#main})
 * in-process against the real Postgres, then drives it exactly like the real client does -
 * through the RMI stubs from {@code common-api} - rather than calling server-side DAOs/repositories
 * directly. This exercises the same client/server boundary the shipped jars use.
 */
@Tag("integration")
@DisplayName("Server <-> client RMI round trip (real Postgres, real RMI registry)")
class ServerClientRmiIntegrationTest {

    private static AuthServiceInter authService;
    private static RestaurantServiceInter restaurantService;

    private User owner;
    private Location location;
    private Restaurant restaurant;

    @BeforeAll
    static void startServerAndConnect() throws Exception {
        assertTrue(Database.initTables(), "schema init failed - is `docker compose up -d` running?");
        assertTrue(Database.initialiseConstants(), "constants seed failed");

        Thread serverThread = new Thread(() -> TheKnifeServer.main(new String[0]), "test-the-knife-server");
        serverThread.setDaemon(true);
        serverThread.start();

        Exception lastFailure = null;
        for (int attempt = 0; attempt < 50; attempt++) {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                authService = (AuthServiceInter) registry.lookup("auth");
                restaurantService = (RestaurantServiceInter) registry.lookup("restaurant");
                return;
            } catch (Exception e) {
                lastFailure = e;
                Thread.sleep(200);
            }
        }
        fail("Server RMI registry did not come up within 10s: " + lastFailure);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (restaurant != null) {
            // must unlink before delete: the FK from user_restaurants blocks a plain delete()
            restaurantService.unregisterOwner(owner.getId(), restaurant.getId());
        }
        if (owner != null) {
            DbCleanup.deleteUser(owner.getId());
        }
        if (location != null) {
            DbCleanup.deleteLocation(location.getLatitude(), location.getLongitude());
        }
    }

    @Test
    @DisplayName("register() an owner and save()/findById() a restaurant through the real RMI services")
    void registerOwnerAndPublishRestaurant_roundTripsThroughRmiAndDb() throws Exception {
        location = new Location(Nation.ITALY, "Test City", 45.1234, 9.1234, "Via Test 1");

        User registered = authService.register(
                "it-" + UUID.randomUUID(), "Sup3rSecret!42", "Mario", "Rossi",
                LocalDate.of(1985, 5, 20), location, true);

        assertNotNull(registered);
        assertInstanceOf(Owner.class, registered, "registering with isOwner=true must return an Owner");
        owner = registered;

        restaurant = new Restaurant(UUID.randomUUID(), "IT Restaurant " + UUID.randomUUID(),
                "A place created by the integration test", "https://example.com",
                (Owner) registered, "+39 000 0000", location,
                PriceRange.EXPENSIVE, true, false, Award.ONE_STAR, false,
                Set.of(CuisineType.ITALIAN), Set.of("WiFi"));

        // registerOwner() both persists the restaurant and links it to its owner in one call
        assertTrue(restaurantService.registerOwner(restaurant, owner.getId()),
                "registerOwner() must persist and link the restaurant to its owner over RMI");

        Restaurant fetched = restaurantService.findById(restaurant.getId());
        assertNotNull(fetched, "restaurant must be readable back through the same RMI service");
        assertEquals(restaurant.getName(), fetched.getName());
        assertEquals(restaurant.getOwner().getId(), fetched.getOwner().getId());

        Set<Restaurant> ownedByOwner = restaurantService.findByOwner(owner.getId());
        assertTrue(ownedByOwner.stream().anyMatch(r -> r.getId().equals(restaurant.getId())),
                "findByOwner() must include the restaurant just registered to this owner");
    }

    @Test
    @DisplayName("login() rejects a wrong password for a real registered user")
    void login_withWrongPassword_isRejected() throws Exception {
        location = new Location(Nation.ITALY, "Test City", 46.5678, 10.5678, "Via Test 2");

        owner = authService.register(
                "it-" + UUID.randomUUID(), "CorrectHorseBattery1!", "Luigi", "Verdi",
                LocalDate.of(1990, 3, 3), location, false);
        assertNotNull(owner);

        assertThrows(Exception.class, () -> authService.login(owner.getUsername(), "wrong-password"),
                "login with an incorrect password must not succeed");
    }
}
