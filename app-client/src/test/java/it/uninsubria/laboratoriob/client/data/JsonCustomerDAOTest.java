package it.uninsubria.laboratoriob.client.data;

import it.uninsubria.laboratoriob.api.Constants;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JsonCustomerDAO Tests - Cache L0, JSON L1, RMI L2")
class JsonCustomerDAOTest {

    @TempDir
    Path tempDir;

    private AuthServiceInter mockAuthService;
    private JsonCustomerDAO dao;
    private Customer testCustomer;
    private File usersFile;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthServiceInter.class);
        dao = new JsonCustomerDAO(mockAuthService);

        Location loc = new Location(Nation.ITALY, "Roma", 41.9028, 12.4964, "Via del Corso 10");
        testCustomer = new Customer(UUID.randomUUID(), "luca_verde", "hash789", "salt012",
                "Luca", "Verde", loc, LocalDate.of(1992, 7, 20));
        usersFile = new File(Constants.ROOT, "users.json");
    }

    @AfterEach
    void tearDown() {
        if (usersFile.exists()) usersFile.delete();
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.list() != null && dataDir.list().length == 0)
            dataDir.delete();
    }

    @Test
    @DisplayName("save() and findById() work correctly")
    void testSaveAndFindById() {
        assertTrue(dao.save(testCustomer));
        Optional<Customer> found = dao.findById(testCustomer.getId());
        assertTrue(found.isPresent());
        assertEquals("luca_verde", found.get().getUsername());
    }

    @Test
    @DisplayName("login() calls loginCustomer on RMI fallback")
    void testLoginCallsLoginCustomer() throws RemoteException {
        when(mockAuthService.login("luca_verde", "pass"))
                .thenReturn(testCustomer);

        Optional<Customer> result = dao.login("luca_verde", "pass");
        assertTrue(result.isPresent());
        verify(mockAuthService).login("luca_verde", "pass");
    }

    @Test
    @DisplayName("login() returns empty when RMI returns null")
    void testLoginRMIReturnsNull() throws RemoteException {
        when(mockAuthService.login(anyString(), anyString()))
                .thenReturn(null);

        Optional<Customer> result = dao.login("unknown", "pass");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Shared users.json between owner and customer DAOs")
    void testSharedUsersFile() {
        dao.save(testCustomer);
        assertTrue(usersFile.exists());

        JsonOwnerDAO ownerDao = new JsonOwnerDAO(mockAuthService);
        assertTrue(ownerDao.findAll().isEmpty());
    }
}
