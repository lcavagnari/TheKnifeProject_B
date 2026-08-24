# AGENTS.md

## Project

Java 17 Maven multi-module: "The Knife" restaurant review app.

- **common-api** (`it.uninsubria.laboratoriob:theknifeapi`): shared domain entities (Lombok-annotated), enums, validators, DAO interface. The "core" library.
- **demo3** (`com.example:demo3`): JavaFX 21 GUI client. Depends on common-api.
- Future `server` module planned (not yet present).

Root pom.xml at `merged/pom.xml` aggregates both modules.

## Build & Run

```bash
# From the merged/ directory (where pom.xml lives):
mvn clean compile                  # compile both modules
mvn -pl demo3 -am javafx:run       # launch the GUI
mvn test                           # run tests (common-api only has tests currently)
```

- Always run builds from the `merged/` directory (the one containing the root `pom.xml`).
- `-pl demo3 -am` ensures common-api is built first if needed.

## Key Conventions

- **Lombok everywhere in common-api**: entities use `@Data`, `@Builder`, etc. Getter/setter names may not match field names (e.g., `getWebsiteUrl()`, `isHasDelivery()`, `getCuisinesTypes()`). Check actual getters in common-api before using them in demo3.
- **No module-info.java**: intentionally removed to avoid JPMS + Lombok conflicts.
- **Jackson NOT used in common-api**: common-api entities have no Jackson annotations. JSON serialization is handled in `demo3/data/` repositories with manual mapping.
- **demo3's `data/` package** (`com.example.demo3.data`): `UserRepository`, `RestaurantRepository`, `PasswordUtil`, `Session`. These read/write JSON files locally — designed as a replaceable layer for a future network client.
- **JavaFX FXML files** live in `demo3/src/main/resources/com/example/demo3/`.

## Testing

- Tests exist only in `common-api` (JUnit 5). No tests in demo3 yet.
- Run specific test: `mvn test -pl common-api -Dtest=RestaurantTest`

## Architecture Notes

- common-api's `HeartbeatChannel` and `DAO` are designed for a future client-server split.
- demo3 controllers (`LoginController`, `RegisterController`, `RestaurantsController`, `RestaurantDetailsController`) use the `data/` repository layer, not common-api's DAO directly.
- The `pom.xml` `<modules>` section already has a placeholder comment for a future `server` module.
