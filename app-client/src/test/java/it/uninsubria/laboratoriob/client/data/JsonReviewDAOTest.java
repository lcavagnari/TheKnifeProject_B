package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import org.junit.jupiter.api.*;

import java.io.File;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JsonReviewDAO Tests - Cache L0, JSON L1, RMI L2")
class JsonReviewDAOTest {

    private AuthServiceInter mockAuthService;
    private ReviewServiceInter mockReviewService;
    private JsonCustomerDAO customerDAO;
    private JsonReviewDAO dao;

    private Customer testCustomer;
    private Restaurant testRestaurant;
    private Review testReview;
    private File reviewsFile;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthServiceInter.class);
        mockReviewService = mock(ReviewServiceInter.class);
        customerDAO = new JsonCustomerDAO(mockAuthService);
        dao = new JsonReviewDAO(customerDAO, mockReviewService);

        Location loc = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");
        testCustomer = new Customer(UUID.randomUUID(), "customer1", "hash", "salt",
                "Luca", "Verde", loc, LocalDate.of(1992, 7, 20));

        Owner owner = new Owner(UUID.randomUUID(), "owner1", "hash", "salt",
                "Mario", "Rossi", loc, LocalDate.of(1980, 5, 15));
        testRestaurant = new Restaurant(
                UUID.randomUUID(), "Ristorante Bella Vista", "A great place",
                "https://bella.com", owner, "+39 02 1234567", loc,
                PriceRange.EXPENSIVE, true, true, Award.ONE_STAR, true,
                Set.of(CuisineType.ITALIAN), Set.of("WiFi"));

        testReview = new Review(testRestaurant, testCustomer, 5, LocalDateTime.now(),
                "Excellent food", null);

        reviewsFile = new File(Constants.ROOT, "reviews.json");
    }

    @AfterEach
    void tearDown() {
        if (reviewsFile.exists()) reviewsFile.delete();
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.list() != null && dataDir.list().length == 0)
            dataDir.delete();
    }

    // --- L0: ConcurrentHashMap cache ---

    @Test
    @DisplayName("save() populates cache")
    void testSavePopulatesCache() {
        assertTrue(dao.save(testReview));
        assertTrue(dao.findById(testReview.getId()).isPresent());
    }

    @Test
    @DisplayName("save() returns false for duplicate")
    void testSaveDuplicateReturnsFalse() {
        assertTrue(dao.save(testReview));
        assertFalse(dao.save(testReview));
    }

    @Test
    @DisplayName("save() returns false for null")
    void testSaveNullReturnsFalse() {
        assertFalse(dao.save(null));
    }

    @Test
    @DisplayName("update() modifies cache entry")
    void testUpdateModifiesEntry() {
        dao.save(testReview);
        Review updated = new Review(testReview.getId(), testReview.getRestaurant(),
                testReview.getUser(), 4, testReview.getTimestamp(),
                "Good but could be better", null);
        assertTrue(dao.update(updated));

        Review found = dao.findById(testReview.getId()).get();
        assertEquals(4, found.getValue());
    }

    @Test
    @DisplayName("update() returns false for non-existent")
    void testUpdateNonExistentReturnsFalse() {
        assertFalse(dao.update(testReview));
    }

    @Test
    @DisplayName("delete() removes from cache")
    void testDeleteRemovesFromCache() {
        dao.save(testReview);
        assertTrue(dao.delete(testReview.getId()));
        assertFalse(dao.findById(testReview.getId()).isPresent());
    }

    @Test
    @DisplayName("findByRestaurant() returns filtered reviews")
    void testFindByRestaurant() {
        dao.save(testReview);
        Review other = new Review(UUID.randomUUID(), testRestaurant, testCustomer,
                3, LocalDateTime.now(), "Okay", null);
        dao.save(other);

        List<Review> byRestaurant = dao.findByRestaurant(testRestaurant.getId());
        assertEquals(2, byRestaurant.size());
    }

    @Test
    @DisplayName("findByUser() returns filtered reviews")
    void testFindByUser() {
        dao.save(testReview);
        Customer other = new Customer(UUID.randomUUID(), "cust2", "hash", "salt",
                "Other", "Cust", null, LocalDate.of(1995, 1, 1));
        Review otherReview = new Review(UUID.randomUUID(), testRestaurant, other,
                4, LocalDateTime.now(), "Nice", null);
        dao.save(otherReview);

        List<Review> byUser = dao.findByUser(testCustomer.getId());
        assertEquals(1, byUser.size());
    }

    @Test
    @DisplayName("findAll(offset, limit) paginates correctly")
    void testFindAllPaginated() {
        for (int i = 0; i < 8; i++) {
            dao.save(new Review(UUID.randomUUID(), testRestaurant, testCustomer,
                    i % 5 + 1, LocalDateTime.now(), "Review " + i, null));
        }

        List<Review> page = dao.findAll(0, 3);
        assertEquals(3, page.size());

        List<Review> beyond = dao.findAll(20, 5);
        assertTrue(beyond.isEmpty());
    }

    @Test
    @DisplayName("count() returns correct count")
    void testCount() {
        assertEquals(0, dao.count());
        dao.save(testReview);
        assertEquals(1, dao.count());
    }

    // --- L1: JSON file persistence ---

    @Test
    @DisplayName("save() persists to reviews.json")
    void testPersistToJSON() {
        dao.save(testReview);
        assertTrue(reviewsFile.exists());
        assertTrue(reviewsFile.length() > 0);
    }

    @Test
    @DisplayName("Cache loads from JSON on first access")
    void testLoadFromJSON() {
        dao.save(testReview);

        JsonReviewDAO newDao = new JsonReviewDAO(customerDAO, mockReviewService);
        assertTrue(newDao.findById(testReview.getId()).isPresent());
    }

    // --- L2: RMI fallback ---

    @Test
    @DisplayName("findAll() falls back to RMI when cache empty")
    void testFindAllRMIFallback() throws RemoteException {
        when(mockReviewService.findAll()).thenReturn(List.of(testReview));

        List<Review> result = dao.findAll();
        assertFalse(result.isEmpty());
        verify(mockReviewService).findAll();
    }

    @Test
    @DisplayName("findByRestaurant() falls back to RMI on cache miss")
    void testFindByRestaurantRMIFallback() throws RemoteException {
        when(mockReviewService.findByRestaurant(testRestaurant.getId()))
                .thenReturn(List.of(testReview));

        List<Review> result = dao.findByRestaurant(testRestaurant.getId());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByUser() falls back to RMI on cache miss")
    void testFindByUserRMIFallback() throws RemoteException {
        when(mockReviewService.findByUser(testCustomer.getId()))
                .thenReturn(List.of(testReview));

        List<Review> result = dao.findByUser(testCustomer.getId());
        assertEquals(1, result.size());
    }

    // --- RMI sync on write operations ---

    @Test
    @DisplayName("save() syncs to server via RMI")
    void testSaveSyncsViaRMI() throws RemoteException {
        when(mockReviewService.save(any())).thenReturn(true);

        dao.save(testReview);
        verify(mockReviewService).save(testReview);
    }

    @Test
    @DisplayName("update() syncs to server via RMI")
    void testUpdateSyncsViaRMI() throws RemoteException {
        when(mockReviewService.update(any())).thenReturn(true);
        dao.save(testReview);

        dao.update(testReview);
        verify(mockReviewService).update(testReview);
    }

    @Test
    @DisplayName("delete() syncs to server via RMI")
    void testDeleteSyncsViaRMI() throws RemoteException {
        when(mockReviewService.delete(any())).thenReturn(true);
        dao.save(testReview);

        dao.delete(testReview.getId());
        verify(mockReviewService).delete(testReview.getId());
    }

    @Test
    @DisplayName("Write operations continue if RMI fails")
    void testWriteContinuesOnRMIFailure() throws RemoteException {
        when(mockReviewService.save(any())).thenThrow(new RemoteException("fail"));

        assertDoesNotThrow(() -> dao.save(testReview));
        assertTrue(dao.findById(testReview.getId()).isPresent());
    }
}
