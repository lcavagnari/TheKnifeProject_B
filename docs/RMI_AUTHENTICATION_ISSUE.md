# Issue: RMI Authentication Layer Implementation

## 1. Background

TheKnifeProject_B is a Java CLI system for Michelin restaurant discovery and management. It uses a client-server architecture with PostgreSQL on the server side and JSON-based local caching on the client. Currently, no RMI/RPC layer exists for server-client data synchronization — the client operates entirely on local JSON files. The TCP heartbeat mechanism is the only existing network communication.

The `ClientDataStore` facade (app-client) was explicitly designed to be replaceable with an RMI implementation:

> *"This class is designed to be easily replaceable with an RMI implementation in the future: the local DAOs can be swapped with remote ones without modifying the code that consumes the data."*
> — `ClientDataStore.java`

## 2. Current State

| Aspect | Status |
|--------|--------|
| Server DAOs | PostgreSQL-backed (CustomerDAO, OwnerDAO, RestaurantDAO, ReviewDAO, LocationDAO) via HikariCP |
| Client DAOs | JSON-file-backed (JsonCustomerDAO, JsonOwnerDAO, JsonRestaurantDAO, JsonReviewDAO, JsonLocationDAO) |
| Login | Client-side only — operates on local JSON cache, not the real database |
| Password hashing | `PasswordHasher` exists on server; commented out in client's `LoginMenu` |
| Remote interfaces | None exist |
| Session management | None exists |
| RMI registry | Not set up |
| Heartbeat | Functional TCP heartbeat (HeartbeatChannel, HeartbeatServer, HeartbeatClient) |

## 3. Architecture Description

> ### Shared Contract Layer
>
> **Remote Interfaces:** The system defines shared interfaces that dictate the remote operations available to clients, explicitly requiring a session token for authorized actions.
>
> **Exception Handling:** Custom security exceptions are established within this shared layer to cleanly communicate invalid or expired sessions back to the caller.
>
> ---
>
> ### Server-Side Infrastructure
>
> **Session Management:** A dedicated manager handles the creation and validation of user sessions, maintaining a thread-safe registry that maps active tokens to unique user identifiers.
>
> **Token Generation:** Tokens are constructed using cryptographically secure random data, which is then safely encoded and given a specific prefix for uniform tracking.
>
> **Service Wrappers:** The remote service implementations encapsulate the underlying data access objects. Every secured method first passes the incoming token to the session manager for validation; if the token is valid, the operation proceeds to the data layer, otherwise, it rejects the request.
>
> **Initialization:** During server startup, the underlying data access components and the session manager are instantiated, injected into the remote services, and finally bound to the registry to await client connections.
>
> ---
>
> ### Client-Side Execution
>
> **Direct Remote Communication:** The client's user interface layer retrieves the service stub directly from the registry and communicates with it without intermediary data access proxies.
>
> **Session State:** A dedicated context object holds the active token (received after a successful login) and provides it as a parameter for all subsequent remote operations.
>
> **Error Translation:** The interface is responsible for catching both network failures and authentication rejections, translating these technical exceptions into readable feedback or prompts for the user.

## 4. Reference Implementation

```java
// ===== common-api: it.uninsubria.laboratoriob.remote.FavouritesService =====
public interface FavouritesService extends Remote {
    boolean addFavourite(String token, UUID restaurantId) throws RemoteException, AuthException;
    boolean removeFavourite(String token, UUID restaurantId) throws RemoteException, AuthException;
    Set<UUID> findFavourites(String token) throws RemoteException, AuthException;
}

// ===== common-api: it.uninsubria.laboratoriob.remote.AuthException =====
public class AuthException extends Exception {
    public AuthException(String message) { super(message); }
}

// ===== app-server: it.uninsubria.laboratoriob.server.SessionManager =====
public class SessionManager {
    private final Map<String, UUID> tokens = new ConcurrentHashMap<>();

    public String createSession(UUID userId) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = "tk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, userId);
        return token;
    }

    public UUID validate(String token) throws AuthException {
        UUID userId = tokens.get(token);
        if (userId == null) throw new AuthException("Invalid or expired session");
        return userId;
    }
}

// ===== app-server: it.uninsubria.laboratoriob.server.FavouritesServiceImpl =====
public class FavouritesServiceImpl extends UnicastRemoteObject implements FavouritesService {
    private final CustomerDAO customerDAO;
    private final SessionManager sessionManager;

    public FavouritesServiceImpl(CustomerDAO customerDAO, SessionManager sessionManager) throws RemoteException {
        super(0, csf, ssf);
        this.customerDAO = customerDAO;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean addFavourite(String token, UUID restaurantId) throws AuthException {
        UUID userId = sessionManager.validate(token);
        return customerDAO.addFavourites(userId, restaurantId);
    }

    @Override
    public boolean removeFavourite(String token, UUID restaurantId) throws AuthException {
        UUID userId = sessionManager.validate(token);
        return customerDAO.removeFavourites(userId, restaurantId);
    }

    @Override
    public Set<UUID> findFavourites(String token) throws AuthException {
        UUID userId = sessionManager.validate(token);
        return customerDAO.findFavourites(userId);
    }
}

// ===== app-server: it.uninsubria.laboratoriob.server.ServerMain, inline in main() =====
CustomerDAO customerDAO = new CustomerDAO();
SessionManager sessionManager = new SessionManager();
FavouritesService favouritesService = new FavouritesServiceImpl(customerDAO, sessionManager);
registry.rebind("FavouritesService", favouritesService);

// ===== app-client bootstrap =====
FavouritesService favouritesStub = (FavouritesService) registry.lookup("FavouritesService");
CustomerMenus customerMenus = new CustomerMenus(favouritesStub, sessionContext, io);
```

## 5. Decisions

| Decision | Answer |
|----------|--------|
| Java version | 17 across all modules (align app-client from 25 to 17) |
| Session expiry | Indefinite for now; `// TODO: configurable TTL` |
| Scope constraint | Implement RMI authentication only. No database changes, no schema modifications, no authorization logic beyond token validation. Only new Java files + refactoring existing ones. |

## 6. Scope Definition

**"Within scope"** means: implement exactly the RMI authentication architecture described in Section 3, using the reference implementation in Section 4 as a pattern. Nothing beyond it.

The service wrappers validate the token and delegate to the existing DAOs. The DAOs themselves are not modified. No new database tables, no schema changes, no new DAO methods.

## 7. Implementation Plan

### Phase 1: Shared Contracts (`common-api`)

Package: `it.uninsubria.laboratoriob.remote`

| # | File | Description |
|---|------|-------------|
| 1 | `AuthException.java` | `extends Exception` |
| 2 | `AuthService.java` | `extends Remote` — `login`, `registerCustomer`, `registerOwner` |
| 3 | `RestaurantService.java` | `extends Remote` — browse/search/CRUD, token as first param |
| 4 | `ReviewService.java` | `extends Remote` — review CRUD + respondToReview, token as first param |
| 5 | `FavouritesService.java` | `extends Remote` — add/remove/find favourites, token as first param |

### Phase 2: Server Infrastructure (`app-server`)

| # | File | Description |
|---|------|-------------|
| 6 | `server/SessionManager.java` | Token map, create/validate, `// TODO: TTL` |
| 7 | `server/service/AuthServiceImpl.java` | Login + register, wraps existing DAOs + PasswordHasher |
| 8 | `server/service/RestaurantServiceImpl.java` | Wraps RestaurantDAO, validates token |
| 9 | `server/service/ReviewServiceImpl.java` | Wraps ReviewDAO, validates token |
| 10 | `server/service/FavouritesServiceImpl.java` | Wraps CustomerDAO, validates token |
| 11 | Modify `TheKnifeServer.java` | Create RMI registry, bind services |

### Phase 3: Client Adaptation (`app-client`)

| # | File | Description |
|---|------|-------------|
| 12 | `client/SessionContext.java` | New — holds token + user |
| 13 | Refactor `Menus.java` | Accept RestaurantService + SessionContext |
| 14 | Refactor `LoginMenu.java` | Use AuthService stub |
| 15 | Refactor `GuestMenus.java` | Use stubs, manage SessionContext |
| 16 | Refactor `CustomerMenus.java` | Use stubs + SessionContext |
| 17 | Refactor `OwnerMenus.java` | Use stubs + SessionContext |
| 18 | Refactor `TheKnifeClient.java` | RMI registry lookups |
| 19 | Fix `app-client/pom.xml` | Java 25 → 17 |

### Execution Order

| Step | Action |
|------|--------|
| 1 | Create common-api remote interfaces + AuthException |
| 2 | Create SessionManager |
| 3 | Create 4 service implementations |
| 4 | Modify TheKnifeServer |
| 5 | Create SessionContext |
| 6 | Refactor Menus base class |
| 7 | Refactor LoginMenu |
| 8 | Refactor GuestMenus |
| 9 | Refactor CustomerMenus |
| 10 | Refactor OwnerMenus |
| 11 | Refactor TheKnifeClient |
| 12 | Fix app-client pom.xml |
| 13 | Build and verify compilation |

## 8. Files to Create

| Module | Path |
|--------|------|
| common-api | `src/main/java/it/uninsubria/laboratoriob/remote/AuthException.java` |
| common-api | `src/main/java/it/uninsubria/laboratoriob/remote/AuthService.java` |
| common-api | `src/main/java/it/uninsubria/laboratoriob/remote/RestaurantService.java` |
| common-api | `src/main/java/it/uninsubria/laboratoriob/remote/ReviewService.java` |
| common-api | `src/main/java/it/uninsubria/laboratoriob/remote/FavouritesService.java` |
| app-server | `src/main/java/it/uninsubria/laboratoriob/server/SessionManager.java` |
| app-server | `src/main/java/it/uninsubria/laboratoriob/server/service/AuthServiceImpl.java` |
| app-server | `src/main/java/it/uninsubria/laboratoriob/server/service/RestaurantServiceImpl.java` |
| app-server | `src/main/java/it/uninsubria/laboratoriob/server/service/ReviewServiceImpl.java` |
| app-server | `src/main/java/it/uninsubria/laboratoriob/server/service/FavouritesServiceImpl.java` |
| app-client | `src/main/java/it/uninsubria/laboratoriob/client/SessionContext.java` |

## 9. Files to Modify

| Module | Path | Changes |
|--------|------|---------|
| app-server | `TheKnifeServer.java` | Add RMI registry creation + service binding |
| app-client | `Menus.java` | Replace ClientDataStore with RestaurantService + SessionContext |
| app-client | `LoginMenu.java` | Use AuthService stub, remove commented-out local hashing |
| app-client | `GuestMenus.java` | Use AuthService + RestaurantService stubs |
| app-client | `CustomerMenus.java` | Use RestaurantService + ReviewService + FavouritesService stubs |
| app-client | `OwnerMenus.java` | Use RestaurantService + ReviewService stubs |
| app-client | `TheKnifeClient.java` | RMI registry lookups, remove ClientDataStore dependency |
| app-client | `pom.xml` | Java version 25 → 17 |
