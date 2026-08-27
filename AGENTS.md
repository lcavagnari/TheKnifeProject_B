# AGENTS.md

## Quick commands

- **Full build**: `mvn clean install`
- **CI build**: `mvn -B -ntp package`
- **Run server**: Execute `TheKnifeServer` main class (module `theknifeserver`)
- **Run server with dataset update**: Add `--update` or `--update <csv-path>` arg
- **Run client**: Execute `TheKnifeClient` main class (module `theknifeclient`)

## Prerequisites

- JDK 17+ (CI uses Temurin 17; run configs reference `corretto-17`)
- Maven 3.9.9+
- PostgreSQL 18 running on `localhost:5432/mydb` (user: `testuser`, pass: `test1234`)
- Start DB: `docker compose up -d`

## Module structure

| Module | Artifact | Builds fat JAR | Notes |
|--------|----------|----------------|-------|
| `common-api` | `theknifeapi` | No | Shared domain model, enums, validators, DAO interface. Has unit tests (JUnit 5). |
| `app-server` | `theknifeserver` | Yes (shade) | PostgreSQL persistence, HikariCP pool, CSV dataset loader, RMI + heartbeat server. |
| `app-client` | `theknifeclient` | Yes (shade) | Jackson JSON serialization, CLI menus, local data cache. **Targets Java 25** (different from parent 17). |

Dependency chain: `app-server` and `app-client` both depend on `common-api`.

## Build quirks

- `app-client/pom.xml` sets `maven.compiler.source/target` to **25** while parent uses 17. If building with JDK 17, `app-client` will fail. CI only runs `mvn package` (tests compile but skip shade issues).
- `maven-shade-plugin` relocates `it.uninsubria.laboratoriob.api` → `it.uninsubria.laboratoriob.libs.theknifeapi` in both fat-JAR modules. If you see `ClassNotFoundException` for API types at runtime, check shade config.
- Lombok is a compile-time annotation processor (configured in parent pom). IDE must have Lombok plugin installed.
- `jcenter.bintray.com` repository is referenced in parent and app-server poms (deprecated; may cause warnings).

## Database

- Schema is created programmatically in `Database.initTables()` (DDL in `app-server/.../utils/Database.java`).
- Reference SQL files in `sql/` directory (`create.sql`, `insert.sql`, etc.) are documentation/manual-use only — the app does NOT read them at runtime.
- Connection pool: HikariCP, max 10 connections, 30s timeout.
- Constant tables (price_range, awards, cuisine_type, services) are seeded by `Database.initialiseConstants()` with `ON CONFLICT DO NOTHING`.
- A system user (`system` / `System Michelin`) is created at startup.

## Server architecture

- `TheKnifeServer.main()` → creates DB schema → seeds constants → optional `--update` → `Loader.initialise()` (loads all data into Repository caches).
- RMI registry on port 1099, heartbeat TCP on port 5555.
- `ServerDataStore` is a thin facade delegating to `RestaurantRepository`, `ReviewRepository`, `UserRepository`.
- Repositories encapsulate `ConcurrentHashMap` caching + DAO access. RMI service impls read from repositories.
- DAO classes in `server.data.dao/`: `CustomerDAO`, `LocationDAO`, `OwnerDAO`, `ReviewDAO`, `RestaurantDAO`, `UserDAO`
- RMI service impls in `server.remote/`: `AuthRemoteImpl`, `RestaurantServiceImpl`, `ReviewServiceImpl`, `FavouriteServiceImpl`

## Testing

- `common-api`: unit tests (JUnit 5 + JUnit 3) for entities and enums.
- `app-client`: unit tests (JUnit 5 + Mockito) for all client DAOs and data flow sync patterns.
- No server integration tests yet.
- Run tests: `mvn test -pl common-api` or `mvn test -pl app-client`

## CI

- GitHub Actions workflow `.github/workflows/maven.yml` runs on push/PR to `master`.
- Runs `mvn -B -ntp package` (build + tests).
- `.github/workflows/maven-publish.yml` publishes to GitHub Packages on release.
