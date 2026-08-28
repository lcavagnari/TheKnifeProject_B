# RMI Hardening Plan — TheKnifeProject Client

**Status: done.** All steps below are implemented; `mvn compile` passes clean. Kept as a record of the design (cooldown + bounded-retry rationale) in case it needs revisiting.

## Situation

The client's RMI connection is fragile: all four service stubs (`restaurant`, `auth`, `review`, `favourite`) are looked up in a single try-catch block. If any one fails, all fail. The stubs are `private final` fields in `ClientDataStore` — they can't be reassigned on reconnect. The `acquireRemoteServices` method has bugs: `Thread.currentThread().wait(500)` throws `IllegalMonitorStateException`, and the retry logic re-declares variables.

## Current Architecture

```
TheKnifeClient
  └─ ClientDataStore
       ├─ JsonRestaurantDAO  (service: RestaurantServiceInter — final)
       ├─ JsonCustomerDAO    (favService: FavouriteServiceInter — final, authService via parent)
       │    └─ extends JsonUserDAO  (authService: AuthServiceInter — final)
       ├─ JsonOwnerDAO       (restaurantService: RestaurantServiceInter — final, authService via parent)
       ├─ JsonReviewDAO      (service: ReviewServiceInter — final)
       └─ JsonLocationDAO    (no remote service)
```

Each DAO stores its service as a `private final` field set in the constructor. When `acquireRemoteServices` is called, the fields can't be reassigned. The DAOs already handle `null` service gracefully (local-only mode with `if (service != null)` guards).

## Goal

1. Individual service acquisition — one failing lookup doesn't affect others.
2. Stubs can be re-acquired at runtime (reconnect scenario), without restarting the client.
3. At callsites: if stub is null, opportunistically try to re-acquire it (subject to the cooldown/retry-cap below), use if available, fall through to local if not.
4. Clean error handling — RMI failure prints message and returns null.
5. **Bounded retries.** A service that keeps failing must not be hammered forever and must not stall callers with a blocking sleep on every call. Concretely:
   - Each service tracks a *cooldown timestamp* and a *failed-attempt counter* in `RmiRepository`.
   - A lookup attempt is only made if the cooldown window (500ms) has elapsed since the last attempt — no `Thread.sleep` in the caller, no blocking every call.
   - After **2 consecutive failed attempts**, the service is marked given-up: further calls return `null` immediately with no registry contact at all, until something explicitly resets it (e.g. a future reconnect action, or app restart). This is what surfaces as "sorry, can't reach the server" to the user instead of a silent infinite retry loop.

## Plan

### 1. `RmiRepository` — `client/utils/RmiRepository.java` (**already implemented, diverged from original draft — this reflects actual state**)

`@UtilityClass` (Lombok). Lookups are **synchronous**, not `CompletableFuture`-based — the async design added no value here (`acquireAll()` already does all 4 lookups over one shared `Registry` connection, which is strictly better than 4 independent futures each opening their own connection) and the throttling/give-up behavior lives in this class regardless of sync/async.

```java
@UtilityClass
public class RmiRepository {
    private static final long COOLDOWN_MS = 500;
    private static final int MAX_FAILED_ATTEMPTS = 2;

    private final AtomicReference<RestaurantServiceInter> restaurantService = new AtomicReference<>();
    private final AtomicReference<AuthServiceInter> authService = new AtomicReference<>();
    private final AtomicReference<ReviewServiceInter> reviewService = new AtomicReference<>();
    private final AtomicReference<FavouriteServiceInter> favouriteService = new AtomicReference<>();
    // each service also has an AtomicLong lastAttempt + AtomicInteger failedAttempts
    private volatile String hostname;
    private volatile int port;

    public void configure(String hostname, int port);
    public void reset(); // clears refs + counters + timestamps, re-arms retries

    public RestaurantServiceInter getRestaurantService();
    public AuthServiceInter getAuthService();
    public ReviewServiceInter getReviewService();
    public FavouriteServiceInter getFavouriteService();

    public RestaurantServiceInter lookupRestaurantService();
    public AuthServiceInter lookupAuthService();
    public ReviewServiceInter lookupReviewService();
    public FavouriteServiceInter lookupFavouriteService();

    public Set<String> acquireAll(); // bulk lookup over one Registry connection, bypasses cooldown/cap (explicit reconnect)

    private <T> T lookup(String name, Class<T> type, AtomicReference<T> ref, AtomicLong lastAttempt, AtomicInteger failedAttempts);
}
```

`lookup()` behavior:
1. If `failedAttempts >= MAX_FAILED_ATTEMPTS` → return `null` immediately, no registry contact (given up).
2. If less than `COOLDOWN_MS` has passed since the last attempt → return `null` immediately (throttled, avoids instant-retry-into-failure).
3. Otherwise attempt the lookup: on success, reset `failedAttempts` to 0 and cache the stub; on failure, increment `failedAttempts`, print the error (and a distinct "giving up" message once the cap is hit).

Caller does: `RmiRepository.lookupRestaurantService()` — non-blocking beyond the RMI call itself, returns stub or null. Error printed internally. Caller does nothing else.

### 2. Make DAO service fields mutable + setters (**done**)

Each DAO's `private final XxxServiceInter service` → `private volatile XxxServiceInter service` with a package-private setter. Actual setter names (already committed, differ from original draft — kept as-is, no functional difference):

| DAO | Field | Setter |
|-----|-------|--------|
| `JsonUserDAO` | `authService` | `setRemoteAuthService(AuthServiceInter)` |
| `JsonRestaurantDAO` | `service` | `setRemoteRestaurantService(RestaurantServiceInter)` |
| `JsonCustomerDAO` | `favService` | `setRemoteFavService(FavouriteServiceInter)` |
| `JsonOwnerDAO` | `restaurantService` | `setRemoteRestaurantService(RestaurantServiceInter)` |
| `JsonReviewDAO` | `service` | `setRemoteReviewService(ReviewServiceInter)` |

### 3. Add `ensureService()` helpers to each DAO

Each DAO gets a private helper that: returns the current stub if not null, otherwise asks `RmiRepository` for a fresh one (which internally applies the cooldown + give-up-after-2-failures logic — **no `Thread.sleep` in the DAO**), caches it locally if successful, returns the result.

```java
// JsonRestaurantDAO example
private RestaurantServiceInter ensureService() {
    RestaurantServiceInter current = service;
    if (current != null) return current;
    RestaurantServiceInter fresh = RmiRepository.lookupRestaurantService();
    if (fresh != null) this.service = fresh;
    return fresh;
}
```

### 4. Update DAO callsites

Every `if (service != null)` guard becomes:

```java
RestaurantServiceInter svc = ensureService();
if (svc != null) {
    // use svc for remote call
} else {
    // fallback to local cache
}
```

Every `RemoteException` catch block nulls the local field, so the next call re-asks `RmiRepository` (which is itself throttled/capped):

```java
} catch (RemoteException e) {
    this.service = null;
    System.err.println("...");
}
```

### 5. Rewrite `ClientDataStore` (currently broken — final-field self-assignment, duplicate `registry` var, no return path, doesn't compile)

- Remove the four service fields entirely (they live in `RmiRepository` now).
- Constructor creates DAOs with `null` services (RmiRepository isn't configured/reachable yet at construction time).
- `acquireRemoteServices()` takes no connection params — `RmiRepository` is already `configure()`d by `TheKnifeClient` before `ClientDataStore` is constructed:
  ```java
  public void acquireRemoteServices() {
      RmiRepository.acquireAll();
      propagateServices();
  }
  ```
- `propagateServices()` reads stubs from `RmiRepository.getXxxService()` and pushes them onto each DAO via its `setRemoteXxx` setter.
- Callable multiple times (e.g. a future manual "reconnect" action would call `RmiRepository.reset()` then this again).

### 6. Update `TheKnifeClient`

```java
RmiRepository.configure(serverHost, rmiPort);
this.dataStore = new ClientDataStore();
dataStore.acquireRemoteServices();
```

Also drop the now-unused imports left over from the old constructor-injection approach (`AuthServiceInter`, `FavouriteServiceInter`, `RestaurantServiceInter`, `ReviewServiceInter`, `LocateRegistry`, `Registry`).

### 7. Files touched

| File | Change |
|------|--------|
| `RmiRepository` | **NEW, done** — static utility, synchronous lookups with cooldown + 2-attempt give-up |
| `ClientDataStore` | Remove service fields, delegate to RmiRepository, add `propagateServices()` — currently doesn't compile, needs full rewrite |
| `JsonRestaurantDAO` | `volatile` field + setter (done) + `ensureService()` + callsite updates |
| `JsonUserDAO` | `volatile` field + setter (done) + `ensureAuthService()` + callsite updates |
| `JsonCustomerDAO` | `volatile` field + setter (done) + `ensureFavService()` + callsite updates |
| `JsonOwnerDAO` | `volatile` field + setter (done) + `ensureRestaurantService()` + callsite updates |
| `JsonReviewDAO` | `volatile` field + setter (done) + `ensureService()` + callsite updates |
| `TheKnifeClient` | Use `RmiRepository.configure()`, remove old imports |
| `LoginMenu` | **Correction: did NOT already use RmiRepository** — was calling the now-removed `dataStore.getAuthService()`. Updated to check `RmiRepository.getAuthService()` then fall back to `RmiRepository.lookupAuthService()`. |
