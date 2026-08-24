# The Knife — progetto unito

Questo è il risultato della fusione di `common-api` (il "cuore" del programma) e
`demo3` (la GUI JavaFX), organizzati ora come **progetto Maven multi-modulo**.

```
theknifeproject/            <- pom.xml aggregatore (nuovo)
├── common-api/              <- invariato nella logica, solo pom.xml aggiornato
│   └── ...
└── demo3/                   <- la GUI, aggiornata per usare common-api
    └── ...
```

## Cosa è cambiato

1. **pom.xml radice**: nuovo, aggrega `common-api` e `demo3` come moduli e
   centralizza le versioni (lombok, jackson, junit) in `dependencyManagement`.
   Nel `<modules>` c'è spazio pronto per un futuro modulo `server`.
2. **common-api/pom.xml**: ora eredita dal parent radice (che prima mancava
   nello zip) e dichiara esplicitamente la dipendenza da Lombok.
3. **demo3/pom.xml**: diventato modulo figlio, aggiunge la dipendenza da
   `it.uninsubria.laboratoriob:theknifeapi` (cioè common-api). Rimosso
   `module-info.java`: mischiare JPMS con Lombok e un modulo non modulare
   avrebbe complicato il build senza reale beneficio in questa fase.
4. **Classi doppione rimosse dalla GUI**: `User.java`, `Restaurant.java`,
   `Location.java` locali a `demo3` sono state eliminate. La GUI ora usa
   direttamente le classi vere di common-api:
   `it.uninsubria.laboratoriob.api.objects.{User, Customer, Owner, Location, Restaurant}`
   e gli enum `UserRole`, `Nation`, `CuisineType`, `PriceRange`, `Award`.
5. **Nuovo package `com.example.demo3.data`** nella GUI:
   - `PasswordUtil`: hashing SHA-256+salt (provvisorio, lato client).
   - `UserRepository`: legge/scrive `Customer`/`Owner` come JSON in
     `data/users/`, con conversione manuale (common-api non è annotata per
     Jackson, e non l'ho toccata per non intaccare "il cuore" del programma).
   - `RestaurantRepository`: stessa idea, per `Restaurant`, leggendo da
     `data/restaurants/`.
   - `Session`: tiene traccia dell'utente loggato in memoria.
6. **Controller aggiornati** (`LoginController`, `RegisterController`,
   `RestaurantsController`, `RestaurantDetailsController`) per usare i nuovi
   repository e i getter reali di common-api (es. `getWebsiteUrl()`,
   `isHasDelivery()`, `getCuisinesTypes()`, ecc. — i nomi Lombok non sono
   sempre identici a quelli che avevi nella classe doppione).

## Cosa NON è ancora stato fatto (prossimo passo)

`common-api` contiene già `HeartbeatChannel` e i `DAO`, pensati per
un'architettura client-server: hai confermato che esisterà un server
separato. Per ora `UserRepository` e `RestaurantRepository` nella GUI
leggono/scrivono ancora file JSON locali — sono pensati apposta come
**livello sostituibile**: quando il server sarà pronto, queste due classi
andranno sostituite da un client di rete (socket, riusando
`HeartbeatChannel` per il battito cardiaco della connessione) che parla con
il server, mantenendo la stessa interfaccia pubblica (`findByUsername`,
`save`, `caricaTutti`, `cerca`) così i controller JavaFX non dovranno
cambiare.

## Come buildare

```bash
mvn clean compile   # dalla cartella radice, compila entrambi i moduli
mvn -pl demo3 -am javafx:run   # avvia la GUI (compila anche common-api se serve)
```

> Nota: in questo ambiente sandbox non ho potuto lanciare una build Maven
> completa (rete limitata, niente accesso a Maven Central), quindi le firme
> dei costruttori usati nella GUI sono state controllate a mano contro il
> codice sorgente di common-api. Ti consiglio di lanciare `mvn clean compile`
> in locale come primo passo per intercettare eventuali refusi.
