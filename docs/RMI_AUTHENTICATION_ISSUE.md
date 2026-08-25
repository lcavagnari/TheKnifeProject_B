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

## 3. Architecture

### 3.1 Design Principle

The client menus keep their regular call path. They still call `dataStore.getRestaurantDAO().findAll()`, `dataStore.getReviewDAO().save(review)`, etc. What changes is the DAO **implementation** behind that interface — instead of reading/writing local JSON files, the new remote DAOs delegate to RMI services on the server. The menus never know the difference.

```
Menu code          ClientDataStore        Remote DAO impl         RMI stub         Server
    |                    |                      |                    |                |
    |-- getDAO() ------->|                      |                    |                |
    |                    |-- new RemoteXxxDAO -->|                    |                |
    |-- dao.findAll() -------------------------->| token=ctx.getToken()|                |
    |                                            |--- stub.findAll(token) ----------->|
    |                                            |                    |  sessionMgr.validate()
    |                                            |                    |  serverDAO.findAll()
    |                                            |<--- result --------|                |
    |<--- result --------------------------------|                    |                |
```

### 3.2 What Is Missing — RMI Infrastructure

#### Remote Interfaces (`common-api`)

Java RMI requires interfaces extending `java.rmi.Remote` with `throws RemoteException` on every method. These define the client-server contract.

**Missing:** No `Remote` interfaces. No `AuthException`.

#### Service Implementations (`app-server`)

Each remote interface needs a server-side implementation extending `UnicastRemoteObject` that:
1. Calls `super(0)` to export on an anonymous port
2. Validates the token via `SessionManager`
3. Delegates to the existing DAO

**Missing:** No `UnicastRemoteObject` subclasses. No service implementations.

#### Session Manager (`app-server`)

A `ConcurrentHashMap<String, UUID>` mapping tokens to userIds:
- `createSession(UUID)` — generates `"tk_"` + Base64(SecureRandom(32))
- `validate(String)` — returns userId or throws `AuthException`

**Missing:** No session management.

#### RMI Registry (`app-server`)

At startup: `LocateRegistry.createRegistry(1099)`, then `registry.rebind(name, serviceImpl)` for each service.

**Missing:** `TheKnifeServer` has no registry setup.

#### Client Remote DAOs (`app-client`)

New DAO implementations that implement the same `DAO<T>` interface but call RMI stubs internally. Each holds a reference to `SessionContext` to inject the token.

**Missing:** Only `Json*DAO` implementations exist. No remote DAO implementations.

#### Session Context (`app-client`)

Holds `String token` and `User user`. Created after successful login. Remote DAOs reference it to get the token for every call.

**Missing:** No session context.

#### Validation — Both Sides

- **Client side:** Validate inputs before they leave the client (username format, password length, etc.) — existing `Validators` class already does this
- **Server side:** Revalidate inputs when they arrive at the service implementation before touching the DAO

### 3.3 Execution Flow

**Login:**
1. User enters username/password in `LoginMenu`
2. `LoginMenu.login(dataStore)` calls `authService.login(username, password)` via RMI
3. Server validates credentials via `PasswordHasher.verify()`, creates session, returns token
4. Client stores token in `SessionContext`

**Subsequent calls (e.g. browse restaurants):**
1. Menu calls `dataStore.getRestaurantDAO().findAll()` — same as before
2. `RemoteRestaurantDAO.findAll()` gets token from `SessionContext`, calls `restaurantService.findAll(token)` via RMI
3. Server validates token via `sessionManager.validate()`, calls `restaurantDAO.findAll()`, returns result
4. Client receives result, menu displays it

**Logout:**
1. Menu returns to `GuestMenus`, `SessionContext` token is cleared

## 4. Architecture Description

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
> **Client DAOs Make the RMI Calls:** The client's menu code follows its regular path — it still calls `dataStore.getXxxDAO().method()`. The DAO implementations transparently delegate to remote service stubs via RMI, injecting the session token from `SessionContext`.
>
> **Session State:** A dedicated context object holds the active token (received after a successful login) and provides it as a parameter for all subsequent remote operations.
>
> **Error Translation:** The client DAOs catch both network failures (`RemoteException`) and authentication rejections (`AuthException`), translating these technical exceptions into readable feedback or prompts for the user.
>
> **Dual Validation:** Inputs are validated on the client before they leave, and revalidated on the server when they arrive.

## 5. Reference Implementation

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
    // TODO: configurable session TTL
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
        super(0);
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

// ===== app-client: it.uninsubria.laboratoriob.client.data.RemoteFavouritesDAO =====
public class RemoteFavouritesDAO implements DAO<Customer> {
    private final FavouritesService stub;
    private final SessionContext session;

    public RemoteFavouritesDAO(FavouritesService stub, SessionContext session) {
        this.stub = stub;
        this.session = session;
    }

    public boolean addFavourite(UUID restaurantId) {
        try {
            return stub.addFavourite(session.getToken(), restaurantId);
        } catch (RemoteException e) {
            IO.printErrorMessage("Connection error: " + e.getMessage());
        } catch (AuthException e) {
            IO.printErrorMessage("Session expired, please log in again.");
        }
        return false;
    }

    public boolean removeFavourite(UUID restaurantId) {
        try {
            return stub.removeFavourite(session.getToken(), restaurantId);
        } catch (RemoteException e) {
            IO.printErrorMessage("Connection error: " + e.getMessage());
        } catch (AuthException e) {
            IO.printErrorMessage("Session expired, please log in again.");
        }
        return false;
    }

    public Set<UUID> findFavourites() {
        try {
            return stub.findFavourites(session.getToken());
        } catch (RemoteException e) {
            IO.printErrorMessage("Connection error: " + e.getMessage());
        } catch (AuthException e) {
            IO.printErrorMessage("Session expired, please log in again.");
        }
        return Set.of();
    }
}

// ===== app-server: it.uninsubria.laboratoriob.server.ServerMain, inline in main() =====
SessionManager sessionManager = new SessionManager();
CustomerDAO customerDAO = new CustomerDAO();
FavouritesService favouritesService = new FavouritesServiceImpl(customerDAO, sessionManager);
registry.rebind("FavouritesService", favouritesService);

// ===== app-client bootstrap =====
FavouritesService favouritesStub = (FavouritesService) registry.lookup("FavouritesService");
SessionContext session = new SessionContext();
RemoteFavouritesDAO remoteFavDao = new RemoteFavouritesDAO(favouritesStub, session);
```

## 6. Interface Specifications

### 6.1 `AuthException` (common-api)

```java
package it.uninsubria.laboratoriob.remote;

public class AuthException extends Exception {
    public AuthException(String message) { super(message); }
}
```

Thrown by every service method when the token is invalid or missing.

### 6.2 `AuthService` (common-api)

```java
package it.uninsubria.laboratoriob.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import it.uninsubria.laboratoriob.api.objects.Location;

public interface AuthService extends Remote {
    String login(String username, String password) throws RemoteException, AuthException;
    String registerCustomer(String username, String password, String firstName,
            String lastName, Location location, LocalDate dateOfBirth) throws RemoteException, AuthException;
    String registerOwner(String username, String password, String firstName,
            String lastName, Location location, LocalDate dateOfBirth) throws RemoteException, AuthException;
}
```

| Method | Parameters | Returns | Server DAO Mapping | Notes |
|--------|-----------|---------|-------------------|-------|
| `login` | `username`, `password` | `String token` | `CustomerDAO.findByUsername()`, `OwnerDAO.findByUsername()`, `PasswordHasher.verify()` | Looks up user in both DAOs, verifies password, creates session, returns token. Server revalidates inputs. |
| `registerCustomer` | all user fields | `String token` | `CustomerDAO.save()` | Creates customer, creates session, returns token. Password hashing done server-side via `PasswordHasher`. Server revalidates inputs. |
| `registerOwner` | all user fields | `String token` | `OwnerDAO.save()` | Creates owner, creates session, returns token. Password hashing done server-side via `PasswordHasher`. Server revalidates inputs. |

### 6.3 `RestaurantService` (common-api)

```java
package it.uninsubria.laboratoriob.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;

public interface RestaurantService extends Remote {
    List<Restaurant> findAll(String token) throws RemoteException, AuthException;
    Restaurant findById(String token, UUID id) throws RemoteException, AuthException;
    List<Restaurant> findByOwner(String token, UUID ownerId) throws RemoteException, AuthException;
    boolean save(String token, Restaurant restaurant) throws RemoteException, AuthException;
    boolean update(String token, Restaurant restaurant) throws RemoteException, AuthException;
    boolean delete(String token, UUID id) throws RemoteException, AuthException;
    boolean updateCuisines(String token, UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException, AuthException;
    boolean updateServices(String token, UUID restaurantId, Set<String> services) throws RemoteException, AuthException;
}
```

| Method | Parameters | Returns | Server DAO Mapping | Notes |
|--------|-----------|---------|-------------------|-------|
| `findAll` | `token` | `List<Restaurant>` | `RestaurantDAO.findAll()` | Token validated first. |
| `findById` | `token`, `id` | `Restaurant` | `RestaurantDAO.findById(id)` | Token validated first. |
| `findByOwner` | `token`, `ownerId` | `List<Restaurant>` | `RestaurantDAO.findByOwner(ownerId)` | Token validated first. |
| `save` | `token`, `restaurant` | `boolean` | `RestaurantDAO.save(restaurant)` | Token validated first. Server revalidates restaurant fields. |
| `update` | `token`, `restaurant` | `boolean` | `RestaurantDAO.update(restaurant)` | Token validated first. Server revalidates restaurant fields. |
| `delete` | `token`, `id` | `boolean` | `RestaurantDAO.delete(id)` | Token validated first. |
| `updateCuisines` | `token`, `restaurantId`, `cuisines` | `boolean` | `RestaurantDAO.updateCuisines(restaurantId, cuisines)` | Token validated first. Server revalidates cuisine set. |
| `updateServices` | `token`, `restaurantId`, `services` | `boolean` | `RestaurantDAO.updateServices(restaurantId, services)` | Token validated first. Server revalidates services set. |

### 6.4 `ReviewService` (common-api)

```java
package it.uninsubria.laboratoriob.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;
import it.uninsubria.laboratoriob.api.objects.Review;

public interface ReviewService extends Remote {
    List<Review> findByRestaurant(String token, UUID restaurantId) throws RemoteException, AuthException;
    boolean save(String token, Review review) throws RemoteException, AuthException;
    boolean update(String token, Review review) throws RemoteException, AuthException;
    boolean delete(String token, UUID id) throws RemoteException, AuthException;
    boolean respondToReview(String token, UUID reviewId, String response) throws RemoteException, AuthException;
}
```

| Method | Parameters | Returns | Server DAO Mapping | Notes |
|--------|-----------|---------|-------------------|-------|
| `findByRestaurant` | `token`, `restaurantId` | `List<Review>` | `ReviewDAO.findByRestaurant(restaurantId)` | Token validated first. |
| `save` | `token`, `review` | `boolean` | `ReviewDAO.save(review)` | Token validated first. Server revalidates review fields. |
| `update` | `token`, `review` | `boolean` | `ReviewDAO.update(review)` | Token validated first. Server revalidates review fields. |
| `delete` | `token`, `id` | `boolean` | `ReviewDAO.delete(id)` | Token validated first. |
| `respondToReview` | `token`, `reviewId`, `response` | `boolean` | `ReviewDAO.findById(reviewId)`, `ReviewDAO.update(review)` | Token validated first. Looks up review, sets reply, updates. Server revalidates response text. |

### 6.5 `FavouritesService` (common-api)

```java
package it.uninsubria.laboratoriob.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

public interface FavouritesService extends Remote {
    boolean addFavourite(String token, UUID restaurantId) throws RemoteException, AuthException;
    boolean removeFavourite(String token, UUID restaurantId) throws RemoteException, AuthException;
    Set<UUID> findFavourites(String token) throws RemoteException, AuthException;
}
```

| Method | Parameters | Returns | Server DAO Mapping | Notes |
|--------|-----------|---------|-------------------|-------|
| `addFavourite` | `token`, `restaurantId` | `boolean` | `CustomerDAO.addFavourites(userId, restaurantId)` | Token validated first; userId extracted from token. |
| `removeFavourite` | `token`, `restaurantId` | `boolean` | `CustomerDAO.removeFavourites(userId, restaurantId)` | Token validated first; userId extracted from token. |
| `findFavourites` | `token` | `Set<UUID>` | `CustomerDAO.findFavourites(userId)` | Token validated first; userId extracted from token. |

## 7. Decisions

| Decision | Answer |
|----------|--------|
| Java version | 17 across all modules (align app-client from 25 to 17) |
| Session expiry | Indefinite for now; `// TODO: configurable TTL` |
| Scope constraint | Implement RMI authentication only. No database changes, no schema modifications, no authorization logic beyond token validation. Only new Java files + refactoring existing ones. |

## 8. Scope Definition

**"Within scope"** means: implement exactly the RMI authentication architecture described in Section 3, using the reference implementation in Section 5 as a pattern. Nothing beyond it.

The service wrappers validate the token and delegate to the existing DAOs. The DAOs themselves are not modified. No new database tables, no schema changes, no new DAO methods.

## 9. Implementation Plan

### Phase 1: Shared Contracts (`common-api`)

Package: `it.uninsubria.laboratoriob.remote`

| # | File | Description |
|---|------|-------------|
| 1 | `AuthException.java` | `extends Exception` — see Section 6.1 |
| 2 | `AuthService.java` | `extends Remote` — see Section 6.2 |
| 3 | `RestaurantService.java` | `extends Remote` — see Section 6.3 |
| 4 | `ReviewService.java` | `extends Remote` — see Section 6.4 |
| 5 | `FavouritesService.java` | `extends Remote` — see Section 6.5 |

### Phase 2: Server Infrastructure (`app-server`)

| # | File | Description |
|---|------|-------------|
| 6 | `server/SessionManager.java` | Token map, create/validate, `// TODO: TTL` |
| 7 | `server/service/AuthServiceImpl.java` | Login + register, wraps existing DAOs + PasswordHasher, revalidates inputs |
| 8 | `server/service/RestaurantServiceImpl.java` | Wraps RestaurantDAO, validates token, revalidates inputs |
| 9 | `server/service/ReviewServiceImpl.java` | Wraps ReviewDAO, validates token, revalidates inputs |
| 10 | `server/service/FavouritesServiceImpl.java` | Wraps CustomerDAO, validates token |
| 11 | Modify `TheKnifeServer.java` | Create RMI registry, bind services |

### Phase 3: Client Remote DAOs (`app-client`)

| # | File | Description |
|---|------|-------------|
| 12 | `client/SessionContext.java` | New — holds token + user |
| 13 | `client/data/RemoteAuthService.java` | Wraps `AuthService` stub, returns token from login/register |
| 14 | `client/data/RemoteRestaurantDAO.java` | Implements restaurant operations via `RestaurantService` stub + token from SessionContext |
| 15 | `client/data/RemoteReviewDAO.java` | Implements review operations via `ReviewService` stub + token from SessionContext |
| 16 | `client/data/RemoteFavouritesDAO.java` | Implements favourites operations via `FavouritesService` stub + token from SessionContext |
| 17 | Modify `ClientDataStore.java` | Accept remote DAOs instead of JSON DAOs (or constructor flag) |
| 18 | Refactor `LoginMenu.java` | Use `RemoteAuthService` instead of local DAO lookup |
| 19 | Refactor `GuestMenus.java` | Create `SessionContext` after login, pass to `ClientDataStore` |
| 20 | Refactor `TheKnifeClient.java` | RMI registry lookups, wire up remote DAOs + SessionContext |
| 21 | Fix `app-client/pom.xml` | Java 25 → 17 |

### Execution Order

| Step | Action |
|------|--------|
| 1 | Create common-api remote interfaces + AuthException |
| 2 | Create SessionManager |
| 3 | Create 4 service implementations |
| 4 | Modify TheKnifeServer |
| 5 | Create SessionContext |
| 6 | Create RemoteAuthService, RemoteRestaurantDAO, RemoteReviewDAO, RemoteFavouritesDAO |
| 7 | Modify ClientDataStore |
| 8 | Refactor LoginMenu |
| 9 | Refactor GuestMenus |
| 10 | Refactor TheKnifeClient |
| 11 | Fix app-client pom.xml |
| 12 | Build and verify compilation |

## 10. Files to Create

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
| app-client | `src/main/java/it/uninsubria/laboratoriob/client/data/RemoteAuthService.java` |
| app-client | `src/main/java/it/uninsubria/laboratoriob/client/data/RemoteRestaurantDAO.java` |
| app-client | `src/main/java/it/uninsubria/laboratoriob/client/data/RemoteReviewDAO.java` |
| app-client | `src/main/java/it/uninsubria/laboratoriob/client/data/RemoteFavouritesDAO.java` |

## 11. Files to Modify

| Module | Path | Changes |
|--------|------|---------|
| app-server | `TheKnifeServer.java` | Add RMI registry creation + service binding |
| app-client | `ClientDataStore.java` | Accept remote DAOs + SessionContext instead of JSON DAOs |
| app-client | `LoginMenu.java` | Use RemoteAuthService, remove commented-out local hashing |
| app-client | `GuestMenus.java` | Wire SessionContext after login, pass to ClientDataStore |
| app-client | `TheKnifeClient.java` | RMI registry lookups, create remote DAOs + SessionContext |
| app-client | `pom.xml` | Java version 25 → 17 |

## 12. Client Menu → DAO → Remote Service Mapping

Menus keep calling the same DAO methods. The remote DAO implementations translate to RMI calls.

### LoginMenu

| Menu Code | Current DAO Call | Remote DAO Call | RMI Method |
|-----------|-----------------|-----------------|------------|
| `LoginMenu.login(dataStore)` | `dataStore.getCustomerDAO().findByUsername(u)` | `RemoteAuthService.login(username, password)` | `AuthService.login(u, p)` → returns token |
| `LoginMenu.register(dataStore)` | `dataStore.getCustomerDAO().save(user)` | `RemoteAuthService.registerCustomer(...)` | `AuthService.registerCustomer(...)` → returns token |

### Menus base (GuestMenus, CustomerMenus, OwnerMenus)

| Menu Code | Current DAO Call | Remote DAO Call | RMI Method |
|-----------|-----------------|-----------------|------------|
| `searchRestaurant()` | `dataStore.getRestaurantDAO().findAll()` | Same call path | `RestaurantService.findAll(token)` |
| `browseRestaurants()` | `dataStore.getRestaurantDAO().findAll()` | Same call path | `RestaurantService.findAll(token)` |

### CustomerMenus

| Menu Code | Current DAO Call | Remote DAO Call | RMI Method |
|-----------|-----------------|-----------------|------------|
| `viewFavourites()` | `customer.getFavouriteRestourants()` + `dataStore.getRestaurantDAO().findAll()` | `dataStore.getFavouritesDAO().findFavourites()` | `FavouritesService.findFavourites(token)` |
| Add review | `dataStore.getReviewDAO().save(review)` | Same call path | `ReviewService.save(token, review)` |
| Update review | `dataStore.getReviewDAO().update(review)` | Same call path | `ReviewService.update(token, review)` |

### OwnerMenus

| Menu Code | Current DAO Call | Remote DAO Call | RMI Method |
|-----------|-----------------|-----------------|------------|
| `viewOwnedRestaurants()` | `owner.getRestaurantsById().values()` | `dataStore.getRestaurantDAO().findByOwner(ownerId)` | `RestaurantService.findByOwner(token, ownerId)` |
| Edit restaurant | `dataStore.getRestaurantDAO().update(restaurant)` | Same call path | `RestaurantService.update(token, restaurant)` |
| Update cuisines | *(local only)* | `dataStore.getRestaurantDAO().updateCuisines(id, set)` | `RestaurantService.updateCuisines(token, id, set)` |
| Update services | *(local only)* | `dataStore.getRestaurantDAO().updateServices(id, set)` | `RestaurantService.updateServices(token, id, set)` |
| Respond to review | `dataStore.getReviewDAO().update(selected)` | Same call path | `ReviewService.respondToReview(token, reviewId, response)` |
| View latest reviews | `owner.getRestaurantsById()` + stream reviews | `dataStore.getRestaurantDAO().findByOwner(id)` + `dataStore.getReviewDAO().findByRestaurant(id)` | `RestaurantService.findByOwner(token, id)` + `ReviewService.findByRestaurant(token, id)` |

## 13. How Java RMI Works (Implementation Reference)

### Server Side

```java
// 1. Create registry (once, at startup)
Registry registry = LocateRegistry.createRegistry(1099);

// 2. Instantiate services (UnicastRemoteObject exports them automatically)
SessionManager sessionManager = new SessionManager();
RestaurantService restaurantService = new RestaurantServiceImpl(restaurantDAO, sessionManager);

// 3. Bind to registry
registry.rebind("RestaurantService", restaurantService);
```

### Client Side

```java
// 1. Get registry reference
Registry registry = LocateRegistry.getRegistry("localhost", 1099);

// 2. Look up stubs
RestaurantService restaurantStub = (RestaurantService) registry.lookup("RestaurantService");

// 3. Create SessionContext
SessionContext session = new SessionContext();

// 4. Create remote DAO (wraps stub + session context)
RemoteRestaurantDAO restaurantDAO = new RemoteRestaurantDAO(restaurantStub, session);

// 5. Menu calls restaurantDAO.findAll() — transparently calls stub.findAll(token)
List<Restaurant> all = restaurantDAO.findAll();
```

### Error Handling Pattern (Remote DAO)

```java
public List<Restaurant> findAll() {
    try {
        return stub.findAll(session.getToken());
    } catch (RemoteException e) {
        IO.printErrorMessage("Connection error: " + e.getMessage());
    } catch (AuthException e) {
        IO.printErrorMessage("Session expired, please log in again.");
    }
    return List.of();
}
```
