package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JsonRestaurantDAO Tests - Cache L0, JSON L1, RMI L2")
class JsonRestaurantDAOTest {

    private RestaurantServiceInter mockService;
    private JsonRestaurantDAO dao;
    private Restaurant testRestaurant;
    private Owner testOwner;
    private File restaurantsFile;

    @BeforeEach
    void setUp() {
        mockService = mock(RestaurantServiceInter.class);
        dao = new JsonRestaurantDAO(mockService);

        Location loc = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");
        testOwner = new Owner(UUID.randomUUID(), "owner1", "hash", "salt",
                "Mario", "Rossi", loc, LocalDate.of(1980, 5, 15));
        Set<CuisineType> cuisines = new HashSet<>(Set.of(CuisineType.ITALIAN));
        testRestaurant = new Restaurant(
                UUID.randomUUID(), "Ristorante Bella Vista", "A great place",
                "https://bella.com", testOwner, "+39 02 1234567", loc,
                PriceRange.EXPENSIVE, true, true, Award.ONE_STAR, true, cuisines, Set.of("WiFi"));
        restaurantsFile = new File(Constants.ROOT, "restaurants.json");
    }

    @AfterEach
    void tearDown() {
        if (restaurantsFile.exists()) restaurantsFile.delete();
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.list() != null && dataDir.list().length == 0)
            dataDir.delete();
    }

    // --- L0: ConcurrentHashMap cache ---

    @Test
    @DisplayName("save() populates cache")
    void testSavePopulatesCache() {
        assertTrue(dao.save(testRestaurant));
        assertTrue(dao.findById(testRestaurant.getId()).isPresent());
    }

    @Test
    @DisplayName("save() returns false for duplicate")
    void testSaveDuplicateReturnsFalse() {
        assertTrue(dao.save(testRestaurant));
        assertFalse(dao.save(testRestaurant));
    }

    @Test
    @DisplayName("save() returns false for null")
    void testSaveNullReturnsFalse() {
        assertFalse(dao.save(null));
    }

    @Test
    @DisplayName("update() modifies cache entry")
    void testUpdateModifiesEntry() {
        dao.save(testRestaurant);
        Restaurant updated = new Restaurant(testRestaurant.getId(), "New Name", "desc",
                "url", testOwner, "phone", testRestaurant.getLocation(),
                PriceRange.ECONOMY, false, false, Award.NONE, false, Set.of(), Set.of());
        assertTrue(dao.update(updated));

        assertEquals("New Name", dao.findById(testRestaurant.getId()).get().getName());
    }

    @Test
    @DisplayName("update() returns false for non-existent")
    void testUpdateNonExistentReturnsFalse() {
        assertFalse(dao.update(testRestaurant));
    }

    @Test
    @DisplayName("delete() removes from cache")
    void testDeleteRemovesFromCache() {
        dao.save(testRestaurant);
        assertTrue(dao.delete(testRestaurant.getId()));
        assertFalse(dao.findById(testRestaurant.getId()).isPresent());
    }

    @Test
    @DisplayName("findByOwner() returns filtered list")
    void testFindByOwner() {
        dao.save(testRestaurant);
        Owner other = new Owner(UUID.randomUUID(), "other", "hash", "salt",
                "Other", "Owner", null, LocalDate.of(1990, 1, 1));
        Restaurant otherRest = new Restaurant(
                UUID.randomUUID(), "Other Place", "", "", other, "",
                testRestaurant.getLocation(), PriceRange.MODERATE, false, false,
                Award.NONE, false, Set.of(), Set.of());
        dao.save(otherRest);

        List<Restaurant> byOwner = dao.findByOwner(testOwner.getId());
        assertEquals(1, byOwner.size());
    }

    @Test
    @DisplayName("updateCuisines() modifies cuisines in cache")
    void testUpdateCuisines() {
        dao.save(testRestaurant);
        Set<CuisineType> newCuisines = Set.of(CuisineType.FRENCH, CuisineType.MEDITERRANEAN);
        assertTrue(dao.updateCuisines(testRestaurant.getId(), newCuisines));

        Restaurant found = dao.findById(testRestaurant.getId()).get();
        assertEquals(newCuisines, found.getCuisinesTypes());
    }

    @Test
    @DisplayName("updateServices() modifies services in cache")
    void testUpdateServices() {
        dao.save(testRestaurant);
        Set<String> newServices = Set.of("Parking", "Outdoor Seating");
        assertTrue(dao.updateServices(testRestaurant.getId(), newServices));

        Restaurant found = dao.findById(testRestaurant.getId()).get();
        assertEquals(newServices, found.getServices());
    }

    @Test
    @DisplayName("findAll(offset, limit) paginates correctly")
    void testFindAllPaginated() {
        for (int i = 0; i < 10; i++) {
            dao.save(new Restaurant(UUID.randomUUID(), "Rest" + i, "", "", testOwner, "",
                    testRestaurant.getLocation(), PriceRange.MODERATE, false, false,
                    Award.NONE, false, Set.of(), Set.of()));
        }

        List<Restaurant> page = dao.findAll(0, 5);
        assertEquals(5, page.size());

        List<Restaurant> beyond = dao.findAll(20, 5);
        assertTrue(beyond.isEmpty());
    }

    // --- L1: JSON file persistence ---

    @Test
    @DisplayName("save() persists to restaurants.json")
    void testPersistToJSON() {
        dao.save(testRestaurant);
        assertTrue(restaurantsFile.exists());
        assertTrue(restaurantsFile.length() > 0);
    }

    @Test
    @DisplayName("Cache loads from JSON on first access")
    void testLoadFromJSON() throws IOException {
        dao.save(testRestaurant);

        JsonRestaurantDAO newDao = new JsonRestaurantDAO(mockService);
        assertTrue(newDao.findById(testRestaurant.getId()).isPresent());
    }

    // --- L2: RMI fallback ---

    @Test
    @DisplayName("findById() falls back to RMI on cache miss")
    void testFindByIdRMIFallback() throws RemoteException {
        when(mockService.findById(testRestaurant.getId())).thenReturn(testRestaurant);

        Restaurant found = dao.findById(testRestaurant.getId()).orElse(null);
        assertNotNull(found);
        verify(mockService).findById(testRestaurant.getId());
    }

    @Test
    @DisplayName("findById() surfaces RMI failure as ServiceUnavailableException")
    void testFindByIdRMIFailure() throws RemoteException {
        when(mockService.findById(any())).thenThrow(new RemoteException("fail"));

        assertThrows(ServiceUnavailableException.class, () -> dao.findById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("findAll() falls back to RMI when cache empty")
    void testFindAllRMIFallback() throws RemoteException {
        when(mockService.findAll(0, 1000)).thenReturn(Set.of(testRestaurant));

        List<Restaurant> result = dao.findAll();
        assertFalse(result.isEmpty());
        verify(mockService).findAll(0, 1000);
    }

    @Test
    @DisplayName("findByOwner() falls back to RMI when no local match")
    void testFindByOwnerRMIFallback() throws RemoteException {
        when(mockService.findByOwner(testOwner.getId())).thenReturn(Set.of(testRestaurant));

        List<Restaurant> result = dao.findByOwner(testOwner.getId());
        assertEquals(1, result.size());
        verify(mockService).findByOwner(testOwner.getId());
    }

    // --- RMI sync on write operations ---

    @Test
    @DisplayName("save() syncs to server via RMI")
    void testSaveSyncsViaRMI() throws RemoteException {
        when(mockService.save(any())).thenReturn(true);

        dao.save(testRestaurant);
        verify(mockService).save(testRestaurant);
    }

    @Test
    @DisplayName("update() syncs to server via RMI")
    void testUpdateSyncsViaRMI() throws RemoteException {
        when(mockService.update(any())).thenReturn(true);
        dao.save(testRestaurant);

        dao.update(testRestaurant);
        verify(mockService).update(testRestaurant);
    }

    @Test
    @DisplayName("delete() syncs to server via RMI")
    void testDeleteSyncsViaRMI() throws RemoteException {
        when(mockService.delete(any())).thenReturn(true);
        dao.save(testRestaurant);

        dao.delete(testRestaurant.getId());
        verify(mockService).delete(testRestaurant.getId());
    }

    @Test
    @DisplayName("save() persists locally then surfaces RMI failure")
    void testWriteContinuesOnRMIFailure() throws RemoteException {
        when(mockService.save(any())).thenThrow(new RemoteException("fail"));

        assertThrows(ServiceUnavailableException.class, () -> dao.save(testRestaurant));
        assertTrue(dao.findById(testRestaurant.getId()).isPresent());
    }
}
