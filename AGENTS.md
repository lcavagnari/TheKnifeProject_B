# AGENTS.md

## Quick commands

- **Full build**: `mvn clean install` — ⚠️ currently fails (see Build status)
- **CI build**: `mvn -B -ntp package` — also red today
- **Run server**: `TheKnifeServer` main class (module `theknifeserver`)
- **Run server with dataset update**: add `--update` or `--update <csv-path>` arg
- **Run client**: `TheKnifeClient` main class (module `theknifeclient`)

## Prerequisites

- JDK 17+ (CI uses Temurin 17; run configs reference `corretto-17`) — but `app-client` needs JDK 25
- Maven 3.9.9+
- PostgreSQL 18 on `localhost:5432/mydb` (user `testuser` / pass `test1234`), start with `docker compose up -d`
- app-server integration tests need the DB running (see Testing)

## Build status (verified 2026-08-29) — build is red

The reactor does NOT compile cleanly. Two independent blockers:

- **app-server main won't compile**, killing `mvn package`:
  - `utils/Loader.java` imports `server.data.{CustomerDAO,OwnerDAO,RestaurantDAO,ReviewDAO}` but those live in `server.data.dao`; calls one-arg `CsvParser.parseFromDataset(path)` (real signature: `(Path, ServerDataStore)`); and `TheKnifeServer` calls `loader.initialise()` which doesn't exist (only `initialiseMaps()`).
  - `data/remote/{RestaurantServiceImpl,ReviewServiceImpl,FavouriteServiceImpl}.java` are stale legacy copies importing nonexistent `server.data` DAOs. Live RMI impls are in the `server.remote` package — prefer deleting `data/remote/` over fixing it.
- **app-client**: main targets Java 25 (local JDK 17 cannot compile it) and its tests are stale — single-arg `new JsonCustomerDAO(...)` / `new JsonOwnerDAO(...)` (main ctors are package-private, take 2–3 args) and a `JsonLocationDAO` that no longer exists (replaced by `LocationMapper`).

Don't assume a `mvn clean install` failure is unrelated to your change — these files are the landmines. Focused verification:
- `mvn test -pl common-api` — passes
- `mvn test -pl app-server -am` — fails until Loader / `data/remote` are fixed

## Module structure

| Module | Artifact | Fat JAR | Notes |
|--------|----------|---------|-------|
| `common-api` | `theknifeapi` | No | Shared domain model, enums, validators, DAO interface, RMI service interfaces. Clean build + tests. |
| `app-server` | `theknifeserver` | Yes (shade) | PostgreSQL persistence, HikariCP, CSV loader, RMI + heartbeat server. Currently doesn't compile. |
| `app-client` | `theknifeclient` | Yes (shade) | Jackson JSON, CLI menus, local cache. **Targets Java 25** (parent uses 17). Test-compile broken. |
| `app-client-gui` | `theknifeclientgui` | No | **Standalone POM, not in the parent reactor.** JavaFX 17 scaffold (`HelloApplication`), not wired to app logic. Own Maven wrapper. |

Dependency: `app-server` and `app-client` both depend on `common-api`.

## Build quirks

- `maven-shade-plugin` relocates `it.uninsubria.laboratoriob.api` → `it.uninsubria.laboratoriob.libs.theknifeapi` (and libphonenumber → `libs.phonenumbers`) in both fat JARs. `ClassNotFoundException` for API types at runtime → check shade config.
- Lombok is a compile-time annotation processor (configured in parent pom) — IDE needs the Lombok plugin.
- `jcenter.bintray.com` deprecated repo is referenced in parent and app-server poms (warnings only).

## Database

- Schema is created programmatically in `Database.initTables()` (`app-server` `utils/Database.java`); `sql/` files are documentation only, never read at runtime.
- Constant tables seeded by `Database.initialiseConstants()` (`ON CONFLICT DO NOTHING`); a `system`/`System Michelin` user is created at startup.

## Architecture

- Startup: `TheKnifeServer.main()` → schema → constants → optional `--update` (CSV import) → `Loader.initialise()` → RMI registry (port 1099, stubs `auth`/`restaurant`/`review`/`favourite`) + heartbeat TCP (port 5555).
- `ServerDataStore` facade → `RestaurantRepository`/`ReviewRepository`/`UserRepository` (`ConcurrentHashMap` caches + JDBC DAOs). RMI impls in `server.remote/` delegate to it.
- Client: `TheKnifeClient` → `ClientDataStore` → Json DAOs (write-through local `data/` JSON cache) + `RmiRepository` (lazy stub acquisition, 500ms cooldown / 2-attempt cap).
- `common-api` `api/remote/` holds the RMI interfaces both sides compile against.

## Testing

- `common-api`: unit tests (JUnit 5 + legacy JUnit 3) for entities/enums. Green.
- `app-client`: unit tests (JUnit 5 + Mockito) for JSON DAOs / data-flow sync — currently fail to compile (see Build status).
- `app-server`: **integration tests against real PostgreSQL** (`RestaurantRepositoryTest`, `ReviewRepositoryTest`, `UserRepositoryTest`, `ServerDataStoreTest` + `testsupport/DbCleanup`). Require the dockerized DB; won't run until the main compile blockers are fixed.

## CI

- `.github/workflows/maven.yml`: `mvn -B -ntp package` on push/PR to `master`. Red today (blockers above).
- `.github/workflows/maven-publish.yml`: publishes to GitHub Packages on release.