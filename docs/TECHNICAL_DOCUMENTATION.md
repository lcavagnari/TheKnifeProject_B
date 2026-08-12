# The Knife Project — Technical Documentation

## 1. Introduction
The Knife is a Java CLI system for restaurant discovery and management. It uses PostgreSQL for persistence, HikariCP for connection pooling, and a modular architecture with a shared domain API (`common-api`), a standalone executable module, and placeholder client/server modules for future expansion.

---

## 2. Project Structure

The project is organized as a Maven multi-module build with the following modules:

| Module | Artifact ID | Role |
|--------|------------|------|
| `common-api` | `theknifeapi` | Shared domain model, enums, validators, DAO interface |
| `standalone` | `standalone` | Executable CLI application (fat JAR via maven-shade-plugin) |
| `app-server` | `theknifeserver` | Placeholder for future server module |
| `app-client` | `theknifeclient` | Placeholder for future client module |

**Note:** The `standalone` module is treated as an executable (like `app-server`/`app-client`), not as a library module. It produces a self-contained JAR with all dependencies shaded and relocated.

### Module Dependency Graph
```
common-api (theknifeapi)
    ^
    |
standalone (depends on theknifeapi + HikariCP + PostgreSQL driver + SLF4J)
```

---

## 3. Runtime Entry Point and Bootstrapping

The application starts via `TheKnife.main(...)` in the `standalone` module.

### Responsibilities
- Initialize database tables and constant data via `Database.initTables()` and `Database.initialiseConstants()`.
- Optionally run Michelin dataset update with `--update [path]`.
- Load in-memory data from PostgreSQL via `Loader.initialiseMaps()`.
- Start guest menu navigation.

### Diagram: Startup Activity Flow
```mermaid
flowchart TD
    A([Start app]) --> B[Database.initTables]
    B --> C[Database.initialiseConstants]
    C --> D{"--update flag?"}
    D -- Yes --> E[Loader.updateMichelinDataset]
    E --> Z([Exit])
    D -- No --> F[Loader.initialiseMaps]
    F --> G[GuestMenus.openMenu]
    G --> H[Database.shutdown]
    H --> Z
```

---

## 4. Persistence Layer

The persistence layer has been migrated from file-based JSON storage to a PostgreSQL relational database with HikariCP connection pooling.

### Database Schema

The schema is defined in `Database.initTables()` and consists of:

| Table | Purpose | Key |
|-------|---------|-----|
| `price_range` | Price tier lookup (economy, moderate, expensive, luxury) | `id INT PK` |
| `awards` | Michelin award lookup (none, stars, Bib Gourmand, etc.) | `id INT PK` |
| `cuisine_type` | Cuisine type lookup (249 types) | `id INT PK` |
| `services_and_facilities` | Service/facility lookup (WiFi, Parking, etc.) | `id INT PK` |
| `location` | Geographic locations | `(latitude, longitude) PK` |
| `"user"` | Users (both customers and owners) | `UUID PK` |
| `restaurant` | Restaurants | `UUID PK` |
| `user_favorites` | Customer → Restaurant favorites (many-to-many) | `(user_id, restaurant_id) PK` |
| `user_restaurants` | Owner → Restaurant ownership (many-to-many) | `(user_id, restaurant_id) PK` |
| `restaurant_cuisine` | Restaurant → Cuisine type (many-to-many) | `(restaurant_id, type) PK` |
| `restaurant_services` | Restaurant → Service (many-to-many) | `(restaurant_id, service) PK` |
| `review` | Reviews (one per user per restaurant) | `UUID PK`, `UNIQUE(user_id, restaurant_id)` |

### Connection Pool (HikariCP)

Configured in `Database`:
- Max pool size: 10 connections
- Connection timeout: 30 seconds
- Min idle: 2 connections
- PreparedStatement cache: 250 entries, max 2048 bytes

### Diagram: Persistence Component Diagram
```mermaid
flowchart TB
    subgraph Application
        Loader[Loader]
        DAOs[DAO Layer]
    end

    subgraph ConnectionPool[HikariCP]
        Pool[(Connection Pool)]
    end

    subgraph Database[PostgreSQL]
        Tables[(Tables)]
    end

    Loader --> DAOs
    DAOs --> Pool
    Pool --> Tables
    Tables --> Pool
    Pool --> DAOs
    DAOs --> Loader
```

---

## 5. Data Access Objects (DAO)

All DAOs implement the generic `DAO<T>` interface defined in `common-api`:

```java
public interface DAO<T> {
    Optional<T> findById(UUID id);
    List<T> findAll();
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(UUID id);
}
```

### DAO Hierarchy
```mermaid
classDiagram
    class DAO~T~ {
        <<interface>>
        +findById(UUID) Optional~T~
        +findAll() List~T~
        +save(T) boolean
        +update(T) boolean
        +delete(UUID) boolean
    }

    class UserDAO~T~ {
        <<abstract>>
        #LocationDAO locationDAO
        #RestaurantDAO restaurantDAO
        -boolean isOwner
        +findByUsername(String) Optional~T~
        +save(T) boolean
        +update(T) boolean
        #findSpecial(UUID) Set~UUID~
        #addSpecial(UUID, UUID) boolean
        #removeSpecial(UUID, UUID) boolean
    }

    class CustomerDAO {
        +addFavourites(UUID, UUID) boolean
        +removeFavourites(UUID, UUID) boolean
        +findFavourites(UUID) Set~UUID~
    }

    class OwnerDAO {
        +addRestaurant(UUID, UUID) boolean
        +removeRestaurant(UUID, UUID) boolean
        +findRestaurants(UUID) Set~UUID~
    }

    class RestaurantDAO {
        +findByOwner(UUID) List~Restaurant~
        +findCuisines(UUID) Set~CuisineType~
        +findServices(UUID) Set~String~
        +updateCuisines(UUID, Set) boolean
        +updateServices(UUID, Set) boolean
    }

    class ReviewDAO {
        +findByRestaurant(UUID) List~Review~
    }

    class LocationDAO {
        +findByCoordinates(double, double) Optional~Location~
        +update(double, double, Location) boolean
        +delete(double, double) boolean
    }

    DAO~T~ <|.. UserDAO~T~
    DAO~T~ <|.. RestaurantDAO
    DAO~T~ <|.. ReviewDAO
    DAO~T~ <|.. LocationDAO
    UserDAO~T~ <|-- CustomerDAO
    UserDAO~T~ <|-- OwnerDAO
```

---

## 6. Domain Model

The domain model is defined in the `common-api` module and shared across all modules.

### Entity Hierarchy
```mermaid
classDiagram
    class Entity {
        <<abstract>>
        #UUID id
        +Entity(UUID)
        +Entity()
        +equals(Object) boolean
        +hashCode() int
    }

    class User {
        <<abstract>>
        -String name
        -String lastName
        -Location location
        -String username
        -LocalDate dateOfBirth
        -String passwordHash
        -String passwordSalt
        -boolean system
        +getRole() UserRole
    }

    class Customer {
        -Set~UUID~ favouriteRestourants
        +addFavourite(Restaurant) boolean
        +removeFavourite(Restaurant) boolean
    }

    class Owner {
        -Map~UUID,Restaurant~ restaurantsById
        -Map~String,Restaurant~ restaurantsByName
        +addRestaurant(Restaurant) boolean
        +removeRestaurant(Restaurant) boolean
        +renameRestaurant(UUID, String) boolean
    }

    class Restaurant {
        -String name
        -String description
        -String websiteUrl
        -String phone
        -Owner owner
        -Location location
        -PriceRange priceRange
        -boolean hasDelivery
        -boolean hasOnlineBooking
        -Award award
        -boolean greenStar
        -Set~CuisineType~ cuisinesTypes
        -Set~String~ services
        -Map~UUID,Review~ reviews
        +addReview(Review) void
        +removeReview(Review) void
        +addService(String) boolean
        +addCuisineType(CuisineType) boolean
    }

    class Review {
        -LocalDateTime timestamp
        -Restaurant restaurant
        -User user
        -int value
        -String text
        -String reply
    }

    class Location {
        -Nation nation
        -String city
        -String address
        -double latitude
        -double longitude
    }

    Entity <|-- User
    Entity <|-- Restaurant
    Entity <|-- Review
    Entity <|-- Location
    User <|-- Customer
    User <|-- Owner
    Restaurant "1" --> "1" Owner
    Restaurant "1" --> "1" Location
    Restaurant "1" --> "0..*" Review
    Review "1" --> "1" User
    Review "1" --> "1" Restaurant
    Customer "1" --> "0..*" Restaurant : favourites
    Owner "1" --> "0..*" Restaurant : owns
```

### Key Enums
| Enum | Values | Purpose |
|------|--------|---------|
| `UserRole` | `CLIENT`, `OWNER` | Distinguishes user capabilities |
| `Award` | `NONE`, `ONE_STAR`, `TWO_STARS`, `THREE_STARS`, `BIB_GOURMAND`, `SELECTED_RESTAURANTS` | Michelin recognition levels |
| `PriceRange` | `ECONOMY`, `MODERATE`, `EXPENSIVE`, `LUXURY` | Price tier classification |
| `CuisineType` | 249 values | Cuisine categories from around the world |
| `Nation` | ~100 values | Supported countries with ISO codes |

---

## 7. Security

### Password Hashing
Passwords are hashed using PBKDF2 with HMAC-SHA256 via `PasswordHasher`:
- Salt length: 16 bytes (random via `SecureRandom`)
- Iterations: 10,000
- Key length: 256 bits
- Encoding: Base64

### Authentication Flow
1. User provides username and password.
2. System retrieves stored hash and salt from database.
3. `PasswordHasher.verify()` compares the attempted password hash with the stored hash.
4. Maximum 4 login attempts before session termination.

---

## 8. User Interface and Navigation Layer

The CLI interface is implemented in the `standalone` module under `it.uninsubria.laboratoriob.ui`.

### UI Component Hierarchy
```mermaid
flowchart LR
    IO[IO - Console I/O]
    Menus[Menus - Abstract Base]
    Guest[GuestMenus]
    CustomerM[CustomerMenus]
    OwnerM[OwnerMenus]
    Login[LoginMenu]

    Menus --> Guest
    Menus --> CustomerM
    Menus --> OwnerM
    Guest --> Login
    Guest --> IO
    CustomerM --> IO
    OwnerM --> IO
    Login --> IO
```

### Navigation Flow
```mermaid
stateDiagram-v2
    [*] --> Guest
    Guest --> Authenticating: login
    Guest --> Registering: registration
    Registering --> Guest: complete/cancel
    Authenticating --> CustomerMenu: CLIENT login
    Authenticating --> OwnerMenu: OWNER login
    Authenticating --> Guest: failure
    CustomerMenu --> Guest: logout
    OwnerMenu --> Guest: logout
    Guest --> Closed: exit
    CustomerMenu --> Closed: exit
    OwnerMenu --> Closed: exit
    Closed --> [*]
```

---

## 9. Operational Workflows

### 9.1 Browse and Search Restaurants
```mermaid
flowchart TD
    A[Open menu] --> B{Browse or Search?}
    B -- Browse --> C[Show restaurants list from Loader]
    B -- Search --> D[Input restaurant name]
    D --> E{Exists in restaurantsByName?}
    E -- Yes --> F[Show details]
    E -- No --> G[Show error + retry]
    C --> F
```

### 9.2 Owner Creates/Edits a Restaurant
```mermaid
sequenceDiagram
    actor O as Owner
    participant OM as OwnerMenus
    participant IO as IO
    participant R as Restaurant
    participant RD as RestaurantDAO
    participant LD as Loader

    O->>OM: add/edit restaurant
    OM->>IO: collect and validate fields
    OM->>R: instantiate/modify Restaurant
    OM->>RD: save/update restaurant
    OM->>LD: update in-memory maps
    OM-->>O: operation result
```

### 9.3 Client Adds/Edits a Review
```mermaid
sequenceDiagram
    actor C as Client
    participant UM as CustomerMenus
    participant IO as IO
    participant RV as Review
    participant R as Restaurant
    participant RD as ReviewDAO

    C->>UM: choose review action
    UM->>IO: collect score/text
    UM->>RV: create/update review
    UM->>R: addReview()
    UM->>RD: save/update review
    UM-->>C: confirmation
```

### 9.4 Michelin Dataset Update
```mermaid
sequenceDiagram
    participant Main as TheKnife.main
    participant L as Loader
    participant CP as CsvParser
    participant DB as Database
    participant Cache as Loader (in-memory)

    Main->>L: updateMichelinDataset(path)
    L->>CP: parseFromDataset(path)
    CP->>CP: parse CSV lines
    CP->>Cache: addRestaurant (in-memory)
    CP->>DB: save restaurant + location (async)
    CP-->>L: update complete
```

---

## 10. Utilities

### CsvParser
Parses the Michelin restaurant CSV dataset and persists records to the database and in-memory cache. Handles:
- Robust parsing of irregular CSV fields (addresses, phone numbers, coordinates).
- Normalization of cities, nations, awards, cuisine types, and services.
- Creation of a system owner for imported records.

### Loader
Central in-memory data store with `ConcurrentHashMap` for thread-safe access. Provides:
- Read operations (single entity lookup, bulk unmodifiable views).
- Write operations (add, remove, update for restaurants and users).
- Initialization from database via DAOs.

### Database
Manages the PostgreSQL connection pool and schema initialization via HikariCP.

### PasswordHasher
PBKDF2-based password hashing utility with configurable parameters.

---

## 11. Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17+ |
| Build | Maven | 3.9.9+ |
| Database | PostgreSQL | 42.7.7 (driver) |
| Connection Pool | HikariCP | 7.0.2 |
| Logging | SLF4J Simple | 2.0.17 |
| Code Generation | Lombok | 1.18.38 |
| Phone Validation | libphonenumber | 8.13.27 |
| Testing | JUnit 5 | 5.10.2 |

---

## 12. Observations and Improvement Opportunities
- The `standalone` module is the current executable; `app-server` and `app-client` are placeholders for future client-server architecture.
- The `UserDAO` uses a boolean `isOwner` flag to distinguish user types in the same table — a potential refactor to separate tables or use inheritance mapping.
- `LocationDAO` uses composite primary key (latitude, longitude) instead of a UUID, which is non-standard compared to other entities.
- `CsvParser` creates random lat/lon for locations that lack coordinates — this could be improved with geocoding.
- The in-memory `Loader` caches all data at startup — suitable for the current scale but may need pagination or lazy loading for larger datasets.
- Connection pool credentials are hardcoded in `Database.java` — should be externalized to configuration.
