package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonLocationDAO Tests - Local only (no RMI)")
class JsonLocationDAOTest {

    private JsonLocationDAO dao;
    private Location testLocation;
    private File locationsFile;

    @BeforeEach
    void setUp() {
        dao = new JsonLocationDAO();
        testLocation = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");
        locationsFile = new File(Constants.ROOT, "locations.json");
    }

    @AfterEach
    void tearDown() {
        if (locationsFile.exists()) locationsFile.delete();
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.list() != null && dataDir.list().length == 0) {
            dataDir.delete();
        }
    }

    // --- L0: ConcurrentHashMap cache ---

    @Test
    @DisplayName("save() populates cache")
    void testSavePopulatesCache() {
        assertTrue(dao.save(testLocation));
        assertEquals(1, dao.count());
    }

    @Test
    @DisplayName("save() returns false for duplicate coordinates")
    void testSaveDuplicateReturnsFalse() {
        assertTrue(dao.save(testLocation));
        Location sameCoords = new Location(Nation.FRANCE, "Paris", 45.4642, 9.1900, "Rue Test");
        assertFalse(dao.save(sameCoords));
    }

    @Test
    @DisplayName("save() returns false for null")
    void testSaveNullReturnsFalse() {
        assertFalse(dao.save(null));
    }

    @Test
    @DisplayName("findByCoordinates() returns matching location")
    void testFindByCoordinates() {
        dao.save(testLocation);
        Optional<Location> found = dao.findByCoordinates(45.4642, 9.1900);
        assertTrue(found.isPresent());
        assertEquals("Milano", found.get().getCity());
    }

    @Test
    @DisplayName("findByCoordinates() returns empty for non-existent")
    void testFindByCoordinatesReturnsEmpty() {
        assertTrue(dao.findByCoordinates(0.0, 0.0).isEmpty());
    }

    @Test
    @DisplayName("findByCoordinates() uses epsilon comparison")
    void testFindByCoordinatesEpsilon() {
        dao.save(testLocation);
        Optional<Location> found = dao.findByCoordinates(45.4642001, 9.1900001);
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("update() modifies existing location")
    void testUpdateModifiesExisting() {
        dao.save(testLocation);
        Location updated = new Location(Nation.ITALY, "Roma", 45.4642, 9.1900, "Via Nuova");
        assertTrue(dao.update(updated));

        Location found = dao.findByCoordinates(45.4642, 9.1900).get();
        assertEquals("Roma", found.getCity());
    }

    @Test
    @DisplayName("update() returns false for non-existent")
    void testUpdateNonExistentReturnsFalse() {
        assertFalse(dao.update(testLocation));
    }

    @Test
    @DisplayName("deleteByCoordinates() removes location")
    void testDeleteByCoordinates() {
        dao.save(testLocation);
        assertTrue(dao.deleteByCoordinates(45.4642, 9.1900));
        assertEquals(0, dao.count());
    }

    @Test
    @DisplayName("deleteByCoordinates() returns false for non-existent")
    void testDeleteByCoordinatesReturnsFalse() {
        assertFalse(dao.deleteByCoordinates(0.0, 0.0));
    }

    @Test
    @DisplayName("delete(UUID) always returns false (unsupported)")
    void testDeleteByUUIDAlwaysFalse() {
        assertFalse(dao.delete(java.util.UUID.randomUUID()));
    }

    @Test
    @DisplayName("findAll() returns all cached locations")
    void testFindAll() {
        dao.save(testLocation);
        Location loc2 = new Location(Nation.FRANCE, "Paris", 48.8566, 2.3522, "Rue Test");
        dao.save(loc2);

        List<Location> all = dao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll(offset, limit) paginates correctly")
    void testFindAllPaginated() {
        for (int i = 0; i < 5; i++) {
            dao.save(new Location(Nation.ITALY, "City" + i, 45.0 + i, 9.0 + i, "Addr" + i));
        }

        List<Location> page = dao.findAll(1, 2);
        assertEquals(2, page.size());

        List<Location> beyond = dao.findAll(20, 5);
        assertTrue(beyond.isEmpty());
    }

    @Test
    @DisplayName("findById(UUID) always returns empty (unsupported)")
    void testFindByIdAlwaysEmpty() {
        assertTrue(dao.findById(java.util.UUID.randomUUID()).isEmpty());
    }

    // --- L1: JSON file persistence ---

    @Test
    @DisplayName("save() persists to locations.json")
    void testPersistToJSON() {
        dao.save(testLocation);
        assertTrue(locationsFile.exists());
        assertTrue(locationsFile.length() > 0);
    }

    @Test
    @DisplayName("Cache loads from JSON on first access")
    void testLoadFromJSON() {
        dao.save(testLocation);

        JsonLocationDAO newDao = new JsonLocationDAO();
        assertEquals(1, newDao.count());
        assertTrue(newDao.findByCoordinates(45.4642, 9.1900).isPresent());
    }

    @Test
    @DisplayName("count() returns correct count")
    void testCount() {
        assertEquals(0, dao.count());
        dao.save(testLocation);
        assertEquals(1, dao.count());
    }
}
