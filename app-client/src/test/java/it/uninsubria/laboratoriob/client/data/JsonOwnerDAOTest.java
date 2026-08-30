package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JsonOwnerDAO Tests - Cache L0, JSON L1, RMI L2")
class JsonOwnerDAOTest {

    @TempDir
    Path tempDir;

    private AuthServiceInter mockAuthService;
    private JsonOwnerDAO dao;
    private Owner testOwner;
    private File usersFile;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthServiceInter.class);
        dao = new JsonOwnerDAO(mockAuthService);

        Location loc = new Location(Nation.ITALY, "Milano", 45.4642, 9.1900, "Via Garibaldi 5");
        testOwner = new Owner(UUID.randomUUID(), "mario_rossi", "hash123", "salt456",
                "Mario", "Rossi", loc, LocalDate.of(1985, 3, 15));
        usersFile = new File(Constants.ROOT, "users.json");
    }

    @AfterEach
    void tearDown() {
        if (usersFile.exists()) usersFile.delete();
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.list() != null && dataDir.list().length == 0)
            dataDir.delete();
    }

    // --- L0: ConcurrentHashMap cache ---

    @Test
    @DisplayName("save() populates cache and persists to JSON")
    void testSavePopulatesCacheAndPersists() {
        assertTrue(dao.save(testOwner));

        Optional<Owner> found = dao.findById(testOwner.getId());
        assertTrue(found.isPresent());
        assertEquals(testOwner.getUsername(), found.get().getUsername());
    }

    @Test
    @DisplayName("save() returns false for duplicate")
    void testSaveDuplicateReturnsFalse() {
        assertTrue(dao.save(testOwner));
        assertFalse(dao.save(testOwner));
    }

    @Test
    @DisplayName("save() returns false for null")
    void testSaveNullReturnsFalse() {
        assertFalse(dao.save(null));
    }

    @Test
    @DisplayName("update() modifies cache entry")
    void testUpdateModifiesCacheEntry() {
        dao.save(testOwner);

        Owner updated = new Owner(testOwner.getId(), "mario_rossi", "newhash", "newsalt",
                "Mario", "Rossi", testOwner.getLocation(), LocalDate.of(1985, 3, 15));
        assertTrue(dao.update(updated));

        Optional<Owner> found = dao.findById(testOwner.getId());
        assertTrue(found.isPresent());
        assertEquals("newhash", found.get().getPasswordHash());
    }

    @Test
    @DisplayName("update() returns false for non-existent user")
    void testUpdateNonExistentReturnsFalse() {
        assertFalse(dao.update(testOwner));
    }

    @Test
    @DisplayName("delete() removes from cache")
    void testDeleteRemovesFromCache() {
        dao.save(testOwner);
        assertTrue(dao.delete(testOwner.getId()));
        assertFalse(dao.findById(testOwner.getId()).isPresent());
    }

    @Test
    @DisplayName("delete() returns false for non-existent")
    void testDeleteNonExistentReturnsFalse() {
        assertFalse(dao.delete(UUID.randomUUID()));
    }

    @Test
    @DisplayName("findByUsername() returns cached user")
    void testFindByUsername() {
        dao.save(testOwner);
        Optional<Owner> found = dao.findByUsername("mario_rossi");
        assertTrue(found.isPresent());
        assertEquals(testOwner.getId(), found.get().getId());
    }

    @Test
    @DisplayName("findAll() returns all cached users")
    void testFindAll() {
        dao.save(testOwner);
        Owner owner2 = new Owner(UUID.randomUUID(), "luca_bianchi", "hash", "salt",
                "Luca", "Bianchi", testOwner.getLocation(), LocalDate.of(1990, 1, 1));
        dao.save(owner2);

        List<Owner> all = dao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll(offset, limit) paginates correctly")
    void testFindAllPaginated() {
        for (int i = 0; i < 5; i++) {
            dao.save(new Owner(UUID.randomUUID(), "user" + i, "hash", "salt",
                    "Name" + i, "Last", null, LocalDate.of(1990, 1, 1)));
        }

        List<Owner> page = dao.findAll(1, 2);
        assertEquals(2, page.size());

        List<Owner> beyond = dao.findAll(10, 5);
        assertTrue(beyond.isEmpty());
    }

    @Test
    @DisplayName("count() returns correct count")
    void testCount() {
        assertEquals(0, dao.count());
        dao.save(testOwner);
        assertEquals(1, dao.count());
    }

    // --- L1: JSON file persistence ---

    @Test
    @DisplayName("save() persists to users.json file")
    void testPersistToJSON() throws IOException {
        dao.save(testOwner);
        assertTrue(usersFile.exists());

        String content = Files.readString(usersFile.toPath());
        assertTrue(content.contains("mario_rossi"));
    }

    @Test
    @DisplayName("Cache loads from JSON on first access")
    void testLoadFromJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.createArrayNode();
        com.fasterxml.jackson.databind.node.ObjectNode node = mapper.createObjectNode();
        node.put("id", testOwner.getId().toString());
        node.put("username", "from_file");
        node.put("passwordHash", "hash");
        node.put("passwordSalt", "salt");
        node.put("name", "File");
        node.put("lastName", "User");
        node.put("dateOfBirth", "1990-01-01");
        node.put("system", false);
        array.add(node);
        Files.createDirectories(usersFile.toPath().getParent());
        mapper.writerWithDefaultPrettyPrinter().writeValue(usersFile, array);

        JsonOwnerDAO newDao = new JsonOwnerDAO(mockAuthService);
        Optional<Owner> found = newDao.findByUsername("from_file");
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Corrupted JSON file does not crash DAO")
    void testCorruptedJSON() throws IOException {
        Files.createDirectories(usersFile.toPath().getParent());
        Files.writeString(usersFile.toPath(), "NOT VALID JSON {{{");

        JsonOwnerDAO newDao = new JsonOwnerDAO(mockAuthService);
        assertDoesNotThrow(() -> newDao.findAll());
        assertTrue(newDao.findAll().isEmpty());
    }

    @Test
    @DisplayName("Non-array JSON file is handled gracefully")
    void testNonArrayJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Files.createDirectories(usersFile.toPath().getParent());
        mapper.writeValue(usersFile, mapper.createObjectNode().put("key", "value"));

        JsonOwnerDAO newDao = new JsonOwnerDAO(mockAuthService);
        assertTrue(newDao.findAll().isEmpty());
    }

    @Test
    @DisplayName("Missing JSON file creates empty cache")
    void testMissingJSONFile() {
        File noFile = new File(Constants.ROOT, "nonexistent.json");
        assertFalse(noFile.exists());

        JsonOwnerDAO newDao = new JsonOwnerDAO(mockAuthService);
        assertTrue(newDao.findAll().isEmpty());
    }

    // --- L2: RMI login flow ---

    @Test
    @DisplayName("login() returns cached user without RMI call")
    void testLoginReturnsCachedWithoutRMI() throws RemoteException {
        dao.save(testOwner);

        Optional<Owner> result = dao.login("mario_rossi", "password");
        assertTrue(result.isPresent());
        verify(mockAuthService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("login() falls back to RMI on cache miss")
    void testLoginRMIFallback() throws RemoteException {
        when(mockAuthService.login("mario_rossi", "pass"))
                .thenReturn(testOwner);

        Optional<Owner> result = dao.login("mario_rossi", "pass");
        assertTrue(result.isPresent());
        assertEquals(testOwner.getId(), result.get().getId());
        verify(mockAuthService).login("mario_rossi", "pass");
    }

    @Test
    @DisplayName("login() returns empty on RMI failure")
    void testLoginRMIFailure() throws RemoteException {
        when(mockAuthService.login(anyString(), anyString()))
                .thenThrow(new RemoteException("Connection failed"));

        Optional<Owner> result = dao.login("mario_rossi", "pass");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("login() caches RMI result for subsequent calls")
    void testLoginCachesRMISuccess() throws RemoteException {
        when(mockAuthService.login("mario_rossi", "pass"))
                .thenReturn(testOwner);

        dao.login("mario_rossi", "pass");
        dao.login("mario_rossi", "pass");

        verify(mockAuthService, times(1)).login("mario_rossi", "pass");
    }

    // --- RMI sync on save ---

    @Test
    @DisplayName("save() syncs to server via RMI register()")
    void testSaveSyncsViaRMI() throws RemoteException {
        when(mockAuthService.register(any())).thenReturn(testOwner);

        dao.save(testOwner);
        verify(mockAuthService).register(testOwner);
    }

    @Test
    @DisplayName("save() continues if RMI fails")
    void testSaveContinuesOnRMIFailure() throws RemoteException {
        when(mockAuthService.register(any())).thenThrow(new RemoteException("fail"));

        assertDoesNotThrow(() -> dao.save(testOwner));
        assertTrue(dao.findById(testOwner.getId()).isPresent());
    }

}
