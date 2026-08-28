# AGENTS.md

## Project

Java 17 Maven multi-module: "The Knife" restaurant review app.

- **common-api** (`it.uninsubria.laboratoriob:theknifeapi`): shared domain entities (Lombok-annotated), enums, validators, DAO interface, `HeartbeatChannel`. The "core" library.
- **demo3** (`com.example:demo3`): JavaFX 21 GUI client. Depends on common-api.
- Future `server` module planned (not yet present).

Root `pom.xml` aggregates both modules.

## Build & Run

```bash
# From the repo root (where pom.xml lives):
mvn clean compile                  # compile both modules
mvn -pl demo3 -am javafx:run       # launch the GUI (mainClass: HelloApplication)
mvn test                           # run tests (common-api only)
mvn -pl common-api test            # run common-api tests only
mvn -pl common-api test -Dtest=RestaurantTest  # single test class
```

- `-pl demo3 -am` ensures common-api is built first if needed.
- The javafx-maven-plugin entry point is `com.example.demo3.HelloApplication`, not `Launcher.java`.

## Key Conventions

- **Lombok in common-api only**: entities use `@Data`, `@Builder`, etc. Getter/setter names may not match field names (e.g., `getWebsiteUrl()`, `isHasDelivery()`, `getCuisinesTypes()`). Always check actual getters before referencing them in demo3.
- **No module-info.java**: intentionally removed to avoid JPMS + Lombok conflicts.
- **Jackson NOT used in common-api**: entities have no Jackson annotations. JSON serialization is manual in `demo3/data/` repositories.
- **demo3's `data/` package** (`com.example.demo3.data`): `UserRepository`, `RestaurantRepository`, `PasswordUtil`, `Session`. Read/write JSON files locally — designed as a replaceable layer for a future network client.
- **JavaFX FXML files** live in `demo3/src/main/resources/com/example/demo3/`.

## Testing

- Tests exist only in `common-api` (JUnit 5). No tests in demo3.
- Run: `mvn -pl common-api test` or the single-class command above.

## Architecture Notes

- `HeartbeatChannel` and `DAO` in common-api are designed for a future client-server split.
- demo3 controllers (`LoginController`, `RegisterController`, `RestaurantsController`, `RestaurantDetailsController`) use the `data/` repository layer, not common-api's DAO directly.
- The `pom.xml` `<modules>` section has a placeholder comment for a future `server` module.
