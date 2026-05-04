# The Knife Project — Technical Documentation

## 1. Introduction
The Knife is a Java CLI system for restaurant discovery and management. This document is organized **by application area** so each part (runtime, UI, domain, persistence, operations) has its own explanation and targeted diagrams.

---

## 2. Runtime Entry Point and Bootstrapping
This section describes how the application starts and decides between update-mode and interactive CLI mode.

### Responsibilities
- Parse startup arguments in `TheKnife.main(...)`.
- Optionally run Michelin dataset update.
- Load in-memory data from disk.
- Start guest menu navigation.

### Diagram: Startup Activity Flow
```mermaid
flowchart TD
    A([Start app]) --> B{--update flag?}
    B -- Yes --> C[updateMichelinDataset(path?)]
    C --> Z([Exit])
    B -- No --> D[print "Loading The Knife..."]
    D --> E[Loader.loadFromFile()]
    E --> F[GuestMenus.openMenu()]
    F --> Z
```

### Diagram: Startup Sequence
```mermaid
sequenceDiagram
    participant Main as TheKnife.main
    participant Loader as Loader
    participant Guest as GuestMenus

    Main->>Main: parse args
    alt --update provided
      Main->>Loader: updateMichelinDataset(...)
      Main-->>Main: return
    else normal launch
      Main->>Loader: loadFromFile()
      Main->>Guest: new GuestMenus().openMenu()
    end
```

---

## 3. User Interface and Navigation Layer
This section covers CLI interaction components (`IO`, `Menus`, `GuestMenus`, `UserMenus`, `OwnerMenus`, `Login`) and how users traverse the application.

### Responsibilities
- Console I/O, input validation, and menu rendering.
- Role-based navigation and menu branching.
- Authentication and registration workflows.

### Diagram: UI Module Map
```mermaid
flowchart LR
    IO[IO]
    Login[Login]
    Menus[Menus (abstract)]
    Guest[GuestMenus]
    UserM[UserMenus]
    OwnerM[OwnerMenus]

    Menus --> Guest
    Menus --> UserM
    UserM --> OwnerM
    Guest --> Login
    Guest --> IO
    UserM --> IO
    OwnerM --> IO
    Login --> IO
```

### Diagram: Login Sequence
```mermaid
sequenceDiagram
    actor U as User
    participant G as GuestMenus
    participant L as Login
    participant LD as Loader

    U->>G: choose Login
    G->>L: login()
    loop max 4 attempts
      L->>U: ask username/password
      U-->>L: credentials
      L->>LD: getUsersByName().get(username)
      alt valid + password ok
        L-->>G: authenticated user
      else invalid
        L-->>U: error and retry
      end
    end
```

### Diagram: Registration Sequence
```mermaid
sequenceDiagram
    actor N as New User
    participant L as Login
    participant IO as IO
    participant U as Owner/Client
    participant LD as Loader

    N->>L: register()
    L->>IO: collect role + profile + password
    IO-->>L: validated inputs
    L->>U: instantiate Owner or Client
    L->>LD: add in usersById/usersByName
    L->>U: save()
    L-->>N: registration completed
```

---

## 4. Domain Model (Entities and Business Relationships)
This section documents the core entities and their relationships.

### Responsibilities
- Represent users, restaurants, reviews, and locations.
- Keep entity data consistent and serializable.
- Support role-specific actions (favorites for clients, management for owners).

### Diagram: Domain Class Diagram
```mermaid
classDiagram
    class JsonEntity {
      #UUID id
      #ObjectNode jsonObject
      +save()
      #build()
    }

    class User {
      -username
      -name
      -lastName
      -dateOfBirth
      -location
      +verifyPassword(String) boolean
      +getRole() UserRole
    }

    class Client {
      -Set~UUID~ favouriteRestourants
      +addFavourite(Restaurant) boolean
      +removeFavourite(Restaurant) boolean
    }

    class Owner {
      -Map~UUID,Restaurant~ restaurantsById
      -Map~String,Restaurant~ restaurantsByName
      +addRestaurant(Restaurant) boolean
      +removeRestaurant(Restaurant) boolean
      +renameRestaurant(UUID,String) boolean
    }

    class Restaurant {
      -name
      -owner
      -location
      -Map~UUID,Review~ reviews
      +addReview(Review)
      +removeReview(Review)
      +build()
    }

    class Review
    class Location

    JsonEntity <|-- User
    JsonEntity <|-- Restaurant
    JsonEntity <|-- Review
    JsonEntity <|-- Location

    User <|-- Client
    User <|-- Owner

    Restaurant "1" --> "1" Owner
    Restaurant "1" --> "1" Location
    Restaurant "1" --> "0..*" Review
    Client "1" --> "0..*" Restaurant : favourites
```

### Diagram: Restaurant Lifecycle State Machine
```mermaid
stateDiagram-v2
    [*] --> Created
    Created --> Loaded: deserialized by Loader
    Loaded --> Indexed: inserted in maps
    Indexed --> Updated: owner edits metadata
    Indexed --> Reviewed: addReview()
    Reviewed --> Indexed: remove/edit review
    Updated --> Indexed: build() + save()
    Indexed --> Deleted: owner removes restaurant
    Deleted --> [*]
```

---

## 5. Persistence and Data Management
This section explains file-backed persistence and runtime indexing via `Loader` and `JsonEntity`.

### Responsibilities
- Load users/restaurants from JSON files.
- Maintain fast lookup indexes by ID and name.
- Persist updated entities back to filesystem.

### Diagram: Persistence Component Diagram
```mermaid
flowchart TB
    subgraph Runtime[In-memory runtime]
      U1[(usersById)]
      U2[(usersByName)]
      R1[(restaurantsById)]
      R2[(restaurantsByName)]
    end

    Loader[Loader] --> U1
    Loader --> U2
    Loader --> R1
    Loader --> R2

    JsonEntity[JsonEntity save/build] --> Files[(users/*.json, companies/*.json)]
    Loader --> Files
    Files --> Loader
```

### Diagram: Data Load Sequence
```mermaid
sequenceDiagram
    participant App as TheKnife
    participant L as Loader
    participant FS as FileSystem

    App->>L: loadFromFile()
    L->>FS: list/read users JSON
    FS-->>L: user documents
    L->>L: parse users + fill user maps
    L->>FS: list/read restaurants JSON
    FS-->>L: restaurant documents
    L->>L: parse restaurants + fill restaurant maps
    L-->>App: load complete
```

---

## 6. Operational Workflows
This section isolates the most common day-to-day user operations.

### 6.1 Browse and Search Restaurants
```mermaid
flowchart TD
    A[Open menu] --> B{Browse or Search?}
    B -- Browse --> C[Show restaurants list]
    B -- Search --> D[Input restaurant name]
    D --> E{Exists in restaurantsByName?}
    E -- Yes --> F[Show details]
    E -- No --> G[Show error + retry]
    C --> F
```

### 6.2 Owner Creates a Restaurant
```mermaid
sequenceDiagram
    actor O as Owner
    participant OM as OwnerMenus
    participant IO as IO
    participant R as Restaurant
    participant LD as Loader

    O->>OM: add restaurant
    OM->>IO: collect and validate fields
    OM->>R: instantiate Restaurant
    OM->>O: addRestaurant(R)
    OM->>LD: insert in global restaurant maps
    OM->>R: save()
    OM-->>O: operation result
```

### 6.3 Client Adds/Edits a Review
```mermaid
sequenceDiagram
    actor C as Client
    participant UM as UserMenus
    participant IO as IO
    participant RV as Review
    participant R as Restaurant

    C->>UM: choose review action
    UM->>IO: collect score/text
    UM->>RV: create/update review
    UM->>R: addReview()/update review map
    UM->>R: build() + save()
    UM-->>C: confirmation
```

### 6.4 User Session State
```mermaid
stateDiagram-v2
    [*] --> Guest
    Guest --> Authenticating: login
    Guest --> Registering: registration
    Registering --> Guest: complete/cancel
    Authenticating --> UserMenu: success
    Authenticating --> Guest: failure
    UserMenu --> Guest: logout
    Guest --> Closed: exit
    UserMenu --> Closed: exit
    Closed --> [*]
```

---

## 7. Consolidated Use Case View
This is the cross-section view that connects all roles with supported capabilities.

```mermaid
flowchart LR
    Guest([Guest])
    Client([Client])
    Owner([Owner])

    Browse((Browse restaurants))
    Search((Search restaurants))
    Details((View details))
    Register((Register))
    Login((Login))
    Fav((Manage favourites))
    Reviews((Write/Edit/Delete reviews))
    CreateR((Create restaurant))
    EditR((Edit owned restaurant))
    DeleteR((Delete owned restaurant))

    Guest --> Browse
    Guest --> Search
    Guest --> Details
    Guest --> Register
    Guest --> Login

    Client --> Browse
    Client --> Search
    Client --> Details
    Client --> Fav
    Client --> Reviews

    Owner --> Browse
    Owner --> Search
    Owner --> Details
    Owner --> CreateR
    Owner --> EditR
    Owner --> DeleteR
```

---

## 8. Observations and Improvement Opportunities
- The current approach is simple and effective for a CLI project: in-memory maps + JSON file persistence.
- Domain objects are tightly coupled to serialization (`build()` pattern), which simplifies persistence but couples concerns.
- `Loader` static state is convenient but reduces testability and dependency control.
- A natural evolution path is introducing explicit service/repository layers and integration tests for end-to-end CLI scenarios.
