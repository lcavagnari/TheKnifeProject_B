[licenseImg]: https://img.shields.io/badge/License-MIT-important
[license]: https://github.com/lcavagnari/TheKnifeProject_B/blob/master/LICENSE
[releaseImg]: https://img.shields.io/badge/Version-1.0-blue
[release]: https://github.com/lcavagnari/TheKnifeProject_B/releases/latest

# 🔪 The Knife
---
<br>

[![releaseImg]][release] [![licenseImg]][license]


<br>

## 📑 Indice

# Indice
1. 📝 [Descrizione](#-descrizione)
2. ⚙️ [Caratteristiche Principali](#-caratteristiche-principali)
3. 🏗️ [Architettura](#-architettura)
4. 🖥️ [Requisiti di sistema](#-requisiti-di-sistema)
5. 📦 [Installazione ed Esecuzione](#-installazione-ed-esecuzione)
6. 📚 [Documentazione](#-documentazione)
7. 🧭 [Come Usare](#-come-usare-guida-per-lutente)
    - 👻 [Guest](#-guest-utenti-anonimi)
    - 🍽️ [Cliente](#-cliente)
    - 🧑‍🍳 [Gestore](#-gestore-proprietario)
8. 🛑 [Limiti noti](#-limiti-noti)
9. 🤝 [Contribuire](#-contribuire)
10. 📝 [Licenza](#-licenza)
11. 📬 [Contatti](#-contatti)


---
## 📝 Descrizione

The Knife è un sistema client/server per la scoperta, la gestione e la recensione di ristoranti in stile Michelin. Un server centrale conserva in modo persistente ristoranti, utenti e recensioni su un database PostgreSQL; il client è un'applicazione desktop con interfaccia grafica (JavaFX) che comunica con il server via Java RMI, con una cache locale su file JSON per un'esperienza più reattiva.

Il progetto nasce da un dataset reale di oltre mille ristoranti Michelin ed è pensato per due tipi di utenti registrati — **Cliente** (cerca, recensisce, salva i preferiti) e **Gestore** (inserisce e amministra i propri ristoranti) — oltre alla semplice consultazione da parte di visitatori non registrati.

---

## ⚙️ Caratteristiche Principali

- Architettura **client/server**: il server è l'unica fonte di verità sui dati, il client mantiene una cache locale JSON per le operazioni più frequenti.
- Comunicazione applicativa tramite **Java RMI** (autenticazione, ristoranti, recensioni, preferiti) e monitoraggio della connessione tramite un canale di **heartbeat TCP** indipendente.
- Interfaccia utente **grafica in JavaFX**, con un'interfaccia testuale (CLI) alternativa raggiungibile con il flag `--cli`.
- Persistenza su **PostgreSQL** lato server (via HikariCP), fornito pronto all'uso tramite Docker Compose.
- Gestione di ristoranti, recensioni, utenti (Cliente/Gestore), cucine, premi, fasce di prezzo e servizi offerti.
- Import automatico di un dataset reale di oltre 1000 ristoranti in stile Michelin.
- Password protette con hashing PBKDF2 + salt.

---

## 🏗️ Architettura

Il progetto è organizzato in tre moduli Maven:

| Modulo | Ruolo |
|--------|-------|
| `common-api` | Modello di dominio condiviso, interfacce DAO e interfacce di servizio RMI usate sia dal client sia dal server. |
| `app-server` | Persistenza su PostgreSQL, import del dataset, implementazioni RMI, server di heartbeat. |
| `app-client` | Interfaccia grafica JavaFX (e CLI), cache locale JSON, client RMI e di heartbeat. |

Per i dettagli tecnici completi — diagrammi UML/ER, scelte architetturali, strutture dati e pattern utilizzati — si veda il [Manuale Tecnico](docs/manuale%20tecnico.md).

---

## 🖥️ Requisiti di sistema

| Componente | Versione | Note |
|------------|----------|------|
| JDK | 17+ | Consigliata la distribuzione gratuita "Amazon Corretto 17". |
| Maven | 3.9.9+ | Necessario solo per compilare dai sorgenti. |
| Docker / Docker Compose | recente | Per avviare PostgreSQL senza installazione manuale. |

---

## 📦 Installazione ed Esecuzione

1. **Avviare il database** (dalla cartella principale del progetto):
   ```bash
   docker compose up -d postgres
   ```

2. **Compilare il progetto**:
   ```bash
   mvn package -Dmaven.test.skip=true
   ```
   Vengono generati `app-server/target/theknifeserver-1.0-SNAPSHOT.jar` e `app-client/target/theknifeclient-1.0-SNAPSHOT.jar`.

3. **Avviare il server** (da lasciare sempre in esecuzione):
   ```bash
   java -cp app-server/target/theknifeserver-1.0-SNAPSHOT.jar it.uninsubria.laboratoriob.server.TheKnifeServer --update
   ```
   Il flag `--update` importa, alla prima esecuzione, il dataset Michelin di esempio (`michelin_my_maps.csv`).

4. **Avviare il client**:
   ```bash
   mvn -pl app-client javafx:run
   ```
   In alternativa, con i JAR già pronti: `java -jar app-client/target/theknifeclient-1.0-SNAPSHOT.jar`.

Per una guida passo passo pensata per chi non ha esperienza di programmazione, si veda il [Manuale Utente](docs/manuale%20utente.md).

---

## 📚 Documentazione

- [Manuale Utente](docs/manuale%20utente.md) — guida completa all'uso dell'applicazione, con screenshot.
- [Manuale Tecnico](docs/manuale%20tecnico.md) — architettura, diagrammi UML/ER, strutture dati e scelte progettuali.
- [JavaDoc](https://javacode-docsvault.vercel.app/projects/theknifeproject_b/index.html) — documentazione generata dal codice sorgente.

---

## 🧭 Come Usare (Guida per l'Utente)

Avviato il client, viene mostrata una schermata iniziale da cui accedere, registrarsi, oppure esplorare i ristoranti come ospite.

<p align="center">
  <img src="docs/screenshots/02_home_ospite.png" alt="Schermata iniziale" width="45%">
  <img src="docs/screenshots/05_ricerca_ristoranti.png" alt="Ricerca ristoranti" width="45%">
</p>

### 👻 Guest (utenti anonimi)

Chi non è registrato può comunque:

- 🔍 Cercare ed esplorare il catalogo ristoranti (per nome, cucina o città)
- 📖 Consultare le schede dettagliate dei ristoranti, incluse le recensioni
- 🔐 Accedere in ogni momento a login e registrazione per sbloccare le funzionalità complete

### 🍽️ Cliente

Dopo la registrazione/login come Cliente:

- 📌 Aggiungere o rimuovere un ristorante dai **Preferiti**
- 📄 Leggere tutte le recensioni di un ristorante
- ✏️ Scrivere una recensione, con voto da 1 a 5 stelle e testo libero

### 🧑‍🍳 Gestore (Proprietario)

Dopo la registrazione/login come Gestore:

- ➕ Aggiungere un nuovo ristorante (nome, descrizione, indirizzo, cucina, prezzo, servizi)
- 📝 Modificare i dati di un proprio ristorante
- 🔍 Consultare le recensioni ricevute dai propri ristoranti

<p align="center">
  <img src="docs/screenshots/08_miei_ristoranti.png" alt="I miei ristoranti" width="45%">
  <img src="docs/screenshots/09_nuovo_ristorante.png" alt="Nuovo ristorante" width="45%">
</p>

Per la descrizione completa di ogni funzionalità, con tutti gli screenshot, si veda il [Manuale Utente](docs/manuale%20utente.md).

---

## 🛑 Limiti noti

The Knife è un progetto universitario in sviluppo attivo. I limiti attualmente noti (campo "Nazione" da compilare in italiano, alcune etichette non ancora tradotte, eliminazione di una recensione non ancora funzionante, tra gli altri) sono documentati in dettaglio nella sezione "Limiti della soluzione sviluppata" del [Manuale Utente](docs/manuale%20utente.md#limiti-della-soluzione-sviluppata) e del [Manuale Tecnico](docs/manuale%20tecnico.md#7-limiti-della-soluzione-sviluppata).

---

## 🤝 Contribuire

Per contribuire al progetto:

1. Forkare il repository
2. Creare un branch per la feature/fix: `git checkout -b nome-feature`
3. Commit delle modifiche: `git commit -m "Descrizione della modifica"`
4. Push sul branch: `git push origin nome-feature`
5. Creare una Pull Request descrivendo le modifiche effettuate

---
## 📄 Licenza

Questo progetto è rilasciato sotto licenza MIT. Consultare il file [`LICENSE`](LICENSE) per i dettagli.

---

## 📬 Contatti

| Nome | Ruolo | Matricola |
|------|-------|-----------|
| Luca Cavagnari | Lead / System Architect / Backend | 761291 |
| Francesco Semenzato | Backend | 753593 |
| Matteo Landini | Frontend | 760120 |

Progetto realizzato per il Laboratorio Interdisciplinare B, Università degli Studi dell'Insubria.

*(Creatori di TheFork, perfavore, non denunciateci. ~~Ci hanno costretto~~)*
