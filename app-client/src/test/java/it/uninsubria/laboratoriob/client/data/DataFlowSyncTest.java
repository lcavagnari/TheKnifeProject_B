package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import org.junit.jupiter.api.*;

import java.io.File;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Data Flow Sync Pattern Tests - Verifies cache → JSON → RMI sync behavior")
class DataFlowSyncTest {

    private AuthServiceInter mockAuthService;
    private RestaurantServiceInter mockRestaurantService;
    private ReviewServiceInter mockReviewService;

    private JsonOwnerDAO ownerDAO;
    private JsonCustomerDAO customerDAO;
    private JsonRestaurantDAO restaurantDAO;
    private JsonReviewDAO reviewDAO;

    private Owner testOwner;
    private Customer testCustomer;
    private Restaurant testRestaurant;
    private Review testReview;

    private File usersFile;
    private File restaurantsFile;
    private File reviewsFile;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthServiceInter.class);
        mockRestaurantService = mock(RestaurantServiceInter.class);
        mockReviewService = mock(ReviewServiceInter.class);

        ownerDAO = new JsonOwnerDAO(mockAuthService);
        customerDAO = new JsonCustomerDAO(mockAuthService);
        restaurantDAO = new JsonRestaurantDAO(mockRestaurantService);
        reviewDAO = new JsonReviewDAO(customerDAO, mockReviewService);

        Location loc = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");
        testOwner = new Owner(UUID.randomUUID(), "owner1", "hash", "salt",
                "Mario", "Rossi", loc, LocalDate.of(1980, 5, 15));
        testCustomer = new Customer(UUID.randomUUID(), "customer1", "hash", "salt",
                "Luca", "Verde", loc, LocalDate.of(1992, 7, 20));
        testRestaurant = new Restaurant(
                UUID.randomUUID(), "Ristorante Bella Vista", "A great place",
                "https://bella.com", testOwner, "+39 02 1234567", loc,
                it.uninsubria.laboratoriob.api.enums.PriceRange.EXPENSIVE, true, true,
                it.uninsubria.laboratoriob.api.enums.Award.ONE_STAR, true,
                Set.of(it.uninsubria.laboratoriob.api.enums.CuisineType.ITALIAN), Set.of("WiFi"));
        testReview = new Review(testRestaurant, testCustomer, 5, LocalDateTime.now(),
                "Excellent food", null);

        usersFile = new File(Constants.ROOT, "users.json");
        restaurantsFile = new File(Constants.ROOT, "restaurants.json");
        reviewsFile = new File(Constants.ROOT, "reviews.json");
    }

    @AfterEach
    void tearDown() {
        if (usersFile.exists()) usersFile.delete();
        if (restaurantsFile.exists()) restaurantsFile.delete();
        if (reviewsFile.exists()) reviewsFile.delete();
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.list() != null && dataDir.list().length == 0) {
            dataDir.delete();
        }
    }

    // --- User Flow: login → cache → JSON → RMI ---

    @Test
    @DisplayName("User login flow: cache miss → RMI → cache → JSON")
    void testUserLoginFlow() throws RemoteException {
        when(mockAuthService.login("owner1", "pass")).thenReturn(testOwner);

        // First call: cache miss, goes to RMI
        var result = ownerDAO.login("owner1", "pass");
        assertTrue(result.isPresent());
        verify(mockAuthService).login("owner1", "pass");
        assertTrue(usersFile.exists());

        // Second call: hits cache, no RMI
        result = ownerDAO.login("owner1", "pass");
        assertTrue(result.isPresent());
        verify(mockAuthService, times(1)).login("owner1", "pass");
    }

    @Test
    @DisplayName("User save flow: cache → JSON + RMI register")
    void testUserSaveFlow() throws RemoteException {
        when(mockAuthService.register(any())).thenReturn(testOwner);

        assertTrue(ownerDAO.save(testOwner));
        assertTrue(usersFile.exists());
        verify(mockAuthService).register(testOwner);
    }

    @Test
    @DisplayName("User update flow: cache + JSON only (no RMI)")
    void testUserUpdateFlow() throws RemoteException {
        ownerDAO.save(testOwner);

        Owner updated = new Owner(testOwner.getId(), "owner1", "newhash", "salt",
                "Mario", "Rossi", testOwner.getLocation(), LocalDate.of(1980, 5, 15));
        assertTrue(ownerDAO.update(updated));

        // Verify cache updated
        assertEquals("newhash", ownerDAO.findById(testOwner.getId()).get().getPasswordHash());
        // Verify update() itself made no additional RMI call (only the earlier save() did)
        verify(mockAuthService, times(1)).register(any());
    }

    // --- Restaurant Flow: cache → JSON + RMI ---

    @Test
    @DisplayName("Restaurant save flow: cache → JSON + RMI")
    void testRestaurantSaveFlow() throws RemoteException {
        when(mockRestaurantService.save(any())).thenReturn(true);

        assertTrue(restaurantDAO.save(testRestaurant));
        assertTrue(restaurantsFile.exists());
        verify(mockRestaurantService).save(testRestaurant);
    }

    @Test
    @DisplayName("Restaurant findById: cache hit (no RMI)")
    void testRestaurantFindByIdCacheHit() throws RemoteException {
        restaurantDAO.save(testRestaurant);

        restaurantDAO.findById(testRestaurant.getId());
        verify(mockRestaurantService, never()).findById(any());
    }

    @Test
    @DisplayName("Restaurant findById: cache miss → RMI → cache + JSON")
    void testRestaurantFindByIdCacheMiss() throws RemoteException {
        when(mockRestaurantService.findById(testRestaurant.getId())).thenReturn(testRestaurant);

        var result = restaurantDAO.findById(testRestaurant.getId());
        assertTrue(result.isPresent());
        verify(mockRestaurantService).findById(testRestaurant.getId());
        assertTrue(restaurantsFile.exists());
    }

    @Test
    @DisplayName("Restaurant findAll: cache hit (no RMI)")
    void testRestaurantFindAllCacheHit() throws RemoteException {
        restaurantDAO.save(testRestaurant);

        restaurantDAO.findAll();
        verify(mockRestaurantService, never()).findAll(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Restaurant findAll: cache empty → RMI → cache + JSON")
    void testRestaurantFindAllCacheMiss() throws RemoteException {
        when(mockRestaurantService.findAll(0, 1000)).thenReturn(java.util.Set.of(testRestaurant));

        var result = restaurantDAO.findAll();
        assertFalse(result.isEmpty());
        verify(mockRestaurantService).findAll(0, 1000);
    }

    // --- Review Flow: cache → JSON + RMI ---

    @Test
    @DisplayName("Review save flow: cache → JSON + RMI")
    void testReviewSaveFlow() throws RemoteException {
        when(mockReviewService.save(any())).thenReturn(true);

        assertTrue(reviewDAO.save(testReview));
        assertTrue(reviewsFile.exists());
        verify(mockReviewService).save(testReview);
    }

    @Test
    @DisplayName("Review findByRestaurant: cache miss → RMI → cache")
    void testReviewFindByRestaurantCacheMiss() throws RemoteException {
        when(mockReviewService.findByRestaurant(testRestaurant.getId()))
                .thenReturn(java.util.List.of(testReview));

        var result = reviewDAO.findByRestaurant(testRestaurant.getId());
        assertEquals(1, result.size());
        verify(mockReviewService).findByRestaurant(testRestaurant.getId());
    }

    @Test
    @DisplayName("Review findByRestaurant: cache hit (no RMI)")
    void testReviewFindByRestaurantCacheHit() throws RemoteException {
        reviewDAO.save(testReview);

        reviewDAO.findByRestaurant(testRestaurant.getId());
        verify(mockReviewService, never()).findByRestaurant(any());
    }

    // --- Cross-DAO: Location is local only ---

    @Test
    @DisplayName("Location: local only, no RMI fallback")
    void testLocationLocalOnly() {
        JsonLocationDAO locationDAO = new JsonLocationDAO();
        Location loc = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");

        assertTrue(locationDAO.save(loc));
        assertEquals(1, locationDAO.count());
        assertTrue(locationDAO.findByCoordinates(45.4642, 9.1900).isPresent());
    }

    // --- RMI failure resilience ---

    @Test
    @DisplayName("All write operations continue when RMI fails")
    void testWriteOperationsResilientToRMIFailure() throws RemoteException {
        when(mockAuthService.register(any())).thenThrow(new RemoteException("RMI down"));
        when(mockRestaurantService.save(any())).thenThrow(new RemoteException("RMI down"));
        when(mockReviewService.save(any())).thenThrow(new RemoteException("RMI down"));

        assertDoesNotThrow(() -> ownerDAO.save(testOwner));
        assertDoesNotThrow(() -> restaurantDAO.save(testRestaurant));
        assertDoesNotThrow(() -> reviewDAO.save(testReview));

        assertTrue(ownerDAO.findById(testOwner.getId()).isPresent());
        assertTrue(restaurantDAO.findById(testRestaurant.getId()).isPresent());
        assertTrue(reviewDAO.findById(testReview.getId()).isPresent());
    }

    @Test
    @DisplayName("RMI read failures return empty/local results")
    void testReadRMIFailuresReturnEmptyOrLocal() throws RemoteException {
        when(mockRestaurantService.findById(any())).thenThrow(new RemoteException("RMI down"));
        when(mockReviewService.findByRestaurant(any())).thenThrow(new RemoteException("RMI down"));

        assertTrue(restaurantDAO.findById(UUID.randomUUID()).isEmpty());
        assertTrue(reviewDAO.findByRestaurant(UUID.randomUUID()).isEmpty());
    }
}
