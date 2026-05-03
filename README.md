[licenseImg]: https://img.shields.io/badge/License-MIT-important
[license]: https://github.com/Chiarchiaooo/CommandBlocker/blob/master/LICENSE
[releaseImg]: https://img.shields.io/badge/Version-1.0-blue
[release]: https://github.com/Chiarchiaooo/TheKnifeProject/releases/latest

# 🔪 The Knife
---
<br>

[![releaseImg]][release] [![licenseImg]][license]


<br>

## 📑 Indice

# Indice
1. 📝 [Descrizione](#-descrizione)
2. ⚙️ [Caratteristiche Principali](#-caratteristiche-principali)
3. 🖥️ [Requisiti di sistema](#-requisiti-di-sistema)
4. 📦 [Installazione](#-installazione)
5. 📚 [Documentazione](#-documentazione)
6. 🧭 [Come Usare](#-come-usare-guida-per-lutente)
    - 🔐 [Login](#-login)
    - 🆕 [Registrazione](#-registrazione)
    - 🔎 [Ricerca Ristorante](#-esplora-ristoranti)
    - 👻 [Guest](#-guest-utenti-anonimi)
    - 🍽️ [Cliente](#-client-cliente)
    - 🧑‍🍳 [Proprietario](#-owner-proprietario)
    - 🚪 [Esci](#-esci)
    - 🛑 [Errori Comuni](#-errori-comuni-nessun-problema)
7. 🤝 [Contribuire](#-contribuire)
8. 📝 [Licenza](#-licenza)
9. 📬 [Contatti](#-contatti)
10. 📌 [Note Finali](#-note-finali)


---
## 📝 Descrizione

The Knife è un sistema software per la gestione e la consultazione di ristoranti, recensioni e utenti. Il progetto supporta la serializzazione JSON delle entità, la gestione di diversi tipi di cucina, premi, fasce di prezzo e altre caratteristiche rilevanti per ristoranti.

L'applicazione prevede un'interfaccia a linea di comando (CLI) per l'interazione dell'utente, con funzionalità di login, registrazione, ricerca e visualizzazione dettagliata dei ristoranti.
  
---  

## ⚙️ Caratteristiche Principali

- Gestione di entità principali: `Restaurant`, `Review`, `Location`, `User` e `Owner`.
- Enumerazioni per cucine (`CuisineType`), premi (`Award`), fasce di prezzo (`PriceRange`), e nazionalità (`Nation`).
- Serializzazione e deserializzazione JSON tramite Jackson.
- Input utente tramite CLI con validazioni e messaggi localizzati in italiano.
- Supporto per aggiungere recensioni e gestire servizi offerti dai ristoranti.
- Architettura modulare con classi di utilità e gestione file.

---  

## 🖥️ Requisiti di sistema

| Nome  | Versione | Preferibilmente      |
|-------|----------|----------------------|
| Jdk   | 17+      | `Amazon-corretto 19` |
| Maven | 3.9.9+   | 3.9.9+               |

---  

## 📦 Installazione


### 💿 Metodo automatico

1. Ottenere il jar dai [Releases](https://github.com/Chiarchiaooo/TheKnifeProject/releases/latest)
<br>
<br>
2. Eseguire l'applicazione:

```bash  
java -jar TheKnifeProject-1.0.jar  
```  

---

### 🖥️ Metodo Manuale

1. Clonare la repository:

```bash  
git clone https://github.com/Chiarchiaooo/TheKnifeProject.git
cd TheKnifeProject  
````  

2. Compilare con Maven:

```bash  
mvn clean install  
```  

3. Eseguire l'applicazione:

```bash  
java -jar TheKnifeProject-1.0.jar  
```  
  
---  

## 📚 Documentazione

Visibile attraverso la [Javadoc](https://theknifeproject.projectdocshub.vercel.app/)

  
---  

## 🧭 Come Usare (Guida per l’Utente)

Una volta lanciata l’applicazione (`TheKnifeProject-1.0.jar`), verrà mostrato il **Menu Principale**, da cui puoi accedere a tutte le funzionalità.

### ✅ Registrazione

Se sei un nuovo utente, scegli `Registrazione` e segui le istruzioni.
Puoi scegliere tra:

* **Cliente**: potrai cercare ristoranti, recensirli e salvare i tuoi preferiti.
* **Proprietario**: potrai aggiungere e gestire i tuoi ristoranti.

Verranno richieste alcune informazioni base:

* Nome, Cognome
* Nome utente (almeno 4 caratteri)
* Password (almeno 8 caratteri)
* Città, Nazione, Indirizzo
* Data di nascita

Tutti gli input sono **guidati**: se sbagli, potrai reinserirli.

---

### 🔐 Login

Se hai già un account, scegli `Login`. Inserisci il tuo **nome utente** e **password**.
Se l’autenticazione fallisce, potrai riprovare o tornare al menu principale.

---

### 🔎 Esplora Ristoranti

Dopo l’accesso, potrai:

* **Cercare ristoranti** per:

    * Nome
    * Tipo di cucina (es. ITALIAN, JAPANESE, etc.)
    * Nazione
    * Servizi (es. Delivery, Booking)
    * Fascia di prezzo (€ → €€€€)
    * Premi (es. MICHELIN, GREEN\_STAR)

* **Visualizzare dettagli** completi di ogni ristorante:

    * Descrizione, premi, location, cucina, prezzo, recensioni
    * Proprietario (visibile solo per owner)

---

### 👻 Guest (Utenti anonimi)

Gli utenti *non registrati* (Guest) possono:

- 🔍 Esplorare il catalogo ristoranti in sola lettura
- 📖 Visualizzare informazioni dettagliate sui ristoranti:  
  nome, tipo di cucina, servizi offerti, fascia di prezzo, premi ricevuti, posizione e recensioni
- 🧭 Navigare liberamente tra i risultati, ma **non** possono lasciare recensioni o modificare dati
- 🔐 Accedere in qualsiasi momento alle funzionalità complete effettuando il Login o la Registrazione

---

### 🍽️ Client (Cliente)

Dopo il login come Cliente, potrai:

* 📌 **Aggiungere ai preferiti** un ristorante
* 📄 **Leggere tutte le recensioni** di un ristorante
* ✏️ **Scrivere una recensione** per un ristorante (se non ne hai già scritta una)
* 🛠️ **Modificare la tua recensione** (voto e testo)
* ❌ **Cancellare la tua recensione**
* 🧾 **Visualizzare i tuoi ristoranti preferiti**

Tutte le azioni sono accessibili da un **menu contestuale** dopo aver selezionato un ristorante.

---

### 🧑‍🍳 Owner (Proprietario)

Dopo il login come Proprietario, potrai:

* ➕ **Aggiungere un nuovo ristorante**

    * Nome, descrizione, cucina, premi, servizi, location, prezzo

* 📝 **Modificare i dati di un tuo ristorante**

    * Cambiare qualsiasi campo: nome, tipo cucina, indirizzo, premi, etc.

* ❌ **Eliminare uno dei tuoi ristoranti**

* 🔍 **Visualizzare tutte le recensioni ricevute** per ogni tuo ristorante

Ogni azione è gestita tramite menu interattivi con conferme e messaggi chiari.

---

### 🚪 Esci

Scegli questa opzione per chiudere l’applicazione in sicurezza.

---

### 🛑 Errori Comuni? Nessun problema:

* Ogni campo ha **limiti** (es. minimo caratteri per username o password)
* Il programma **ti guida** se sbagli un input
* Inserisci sempre valori coerenti (es. scegli cucine esistenti o premi validi)

---
  
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

Autore: Luca Cavagnari  
\# Matricola: 761291
Email:  lcavagnari@studenti.uninsubria.it
  
---  

## 🧩 Note Finali

Questo progetto è una dimostrazione di architettura software modulare con particolare attenzione alla serializzazione dati, validazione input utente e gestione di enumerazioni complesse. Ulteriori funzionalità sono pianificate per espandere il sistema.

Progetto creato per il laboratorio A della Università Degli Studi Dell'Insubria.

*(Creatori di TheFork, perfavore, non denunciatemi. ~~Mi hanno costretto~~)*
