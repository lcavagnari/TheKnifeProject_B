package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.IO;
import it.uninsubria.laboratorioa.utils.Loader;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Classe che gestisce i menu principali dell'applicazione.
 * <p>
 * Contiene metodi statici per la navigazione dell'interfaccia utente,
 * come login, registrazione, ricerca di ristoranti e gestione preferiti.
 * <p>
 * Tutti i metodi sono attualmente commentati e servono come struttura
 * base per l'implementazione futura dell'interfaccia.
 * </p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
@UtilityClass
public class Menus {

    /*
    Ecco l’elenco essenziale e minimale delle funzioni richieste dal professore, dedotte dalle specifiche e dalla struttura del progetto:

---

# Funzioni Basiche e Minimali Richieste

### 1. **Login Utente**

* Permette a un utente registrato di autenticarsi nel sistema.

### 2. **Registrazione Utente**

* Permette la creazione di un nuovo utente (cliente o proprietario).

### 3. **Ricerca Ristorante**

* Consente la ricerca di ristoranti tramite filtri base (nome, nazione, tipo di cucina).

### 4. **Visualizzazione Dettagli Ristorante**

* Mostra informazioni dettagliate del ristorante selezionato (nome, indirizzo, servizi, recensioni, ecc.).

### 5. **Modifica Dati Ristorante (solo Owner)**

* Permette al proprietario del ristorante di modificare qualsiasi campo del proprio ristorante.

### 6. **Aggiunta Recensione (Cliente)**

* Consente al cliente autenticato di aggiungere una recensione a un ristorante.

### 7. **Modifica e Cancellazione Recensione (Cliente)**

* Permette al cliente di modificare o cancellare le proprie recensioni esistenti.

### 8. **Salvataggio Automatico**

* Tutte le modifiche devono essere automaticamente salvate su file JSON.

---

# Note

* Le operazioni di modifica sono **protette**: solo l’owner può modificare il proprio ristorante, solo il cliente può modificare/cancellare le proprie recensioni.
* La UI deve gestire chiaramente questi permessi e mostrare solo le opzioni valide.
* Il sistema deve gestire i dati persistenti in file JSON (salvataggio e caricamento).
* Errori di input o mancanze devono essere gestite con messaggi semplici all’utente.

---
*/

    private static User user = null;


    /**
     * Metodo principale del menu dell'applicazione.
     * <p>
     * Mostra un menu con le opzioni di login, registrazione,
     * ricerca di ristoranti e uscita.
     * <p>
     * Al momento, le azioni sono segnaposto (WIP).
     * <p>
     */
    public static void mainMenu() {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n|============= The Knife Main menu =============|",
                    "|===================================================|",
                    "Login",
                    "Register",
                    "Search for a Restaurant",
                    "Browse Restaurants",
                    "Exit"
            );
            int choice = IO.getInt("Enter choice:", 4);

            switch (choice) {
                case 0 -> login();
                case 1 -> register();
                case 2 -> searchRestaurant();
                case 3 -> browseRestaurants();
                case 4 -> exit();
            }
        }
    }

    private static void browseRestaurants() {
        Map<String, Restaurant> restaurants = Loader.getRestaurantsByName();
        if (restaurants == null || restaurants.isEmpty()) {
            IO.printErrorMessage("Nessun ristorante disponibile al momento.");
            return;
        }

        String[] options = restaurants.keySet().toArray(new String[0]);

        IO.printMenu(
                "\n|============= Browse restaurants =============|",
                "|===================================================|",
                options
        );
    }

    /**
     * Metodo che termina l'applicazione stampando un messaggio di saluto.
     */
    private static void exit() {
        IO.clearScreen();
        System.out.println("Goodbye!");

        IO.closeScanner();
        System.exit(0);
    }


    private static void searchRestaurant() {
        IO.clearScreen();

        Restaurant r = null;
        while (r == null) {
            try {
                String name = IO.getUserInput("Enter restaurant name:");
                IO.validateString(name);

                if (!Loader.getRestaurantsByName().containsKey(name))
                    throw new IllegalArgumentException("Nessun ristorante trovato, riprovare");

                r = Loader.getRestaurantsByName().get(name);

            } catch (IllegalArgumentException ex) {
                IO.printErrorMessage(ex.getMessage());

            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return;
            }
        }

        viewRestaurantDetails(r);
    }

    // Mostra le recensioni degli ultimi 7 giorni per i ristoranti dell'owner, in ordine cronologico
    private static void viewOwnerLatestReviews(Owner owner) {
        Map<UUID,Restaurant> restaurants = owner.getRestaurantsById();

        List<Review> recent = restaurants.entrySet().stream()
                .flatMap(r -> r.getValue().getReviews().values().stream())
                .filter(r -> r.getTimestamp() != null && r.getTimestamp().isAfter(LocalDateTime.now().minusDays(7)))
                .sorted(Comparator.comparing(Review::getTimestamp))
                .toList();

        if (recent.isEmpty()) {
            IO.printErrorMessage("Nessuna recensione negli ultimi 7 giorni.");
            IO.getUserInput("Premere 'Enter' per tornare al menu principale.");
            return;
        }

        IO.clearScreen();
        System.out.println("=== Recensioni ricevute negli ultimi 7 giorni ===\n");
        for (Review r : recent) {
            System.out.println(r);
            System.out.println("-".repeat(60));
        }
        IO.getUserInput("\nPremi invio per tornare.");
    }

    // Mostra i ristoranti di proprietà dell’owner
    private static void viewOwnedRestaurants(User user) {
        if (!(user instanceof Owner owner)) {
            IO.printErrorMessage("Accesso negato: funzione riservata ai gestori.");
            return;
        }

        List<Restaurant> list = new ArrayList<>(owner.getRestaurantsById().values());
        if (list == null || list.isEmpty()) {
            IO.printErrorMessage("Non hai ristoranti registrati.");
            IO.getUserInput("Premi invio per tornare.");
            return;
        }

        while (true) {
            IO.clearScreen();
            IO.printMenu(
                    "\n|============= Owned resturants of "+user.getUsername()+" =============|",
                    "|===================================================|",
                    list.toArray(new String[0])
            );

            IO.printMenu("", "", "Torna indietro");

            try {
                int sel = IO.getInt("Seleziona un ristorante da visualizzare:", list.size());
                Menus.viewRestaurantDetails(list.get(sel));
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return;
            }
        }
    }


    private static void userMenu(User user) {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n|============= The Knife Main menu =============|",
                    "|===================================================|",
                    "Search for a Restaurant",
                    "Browse Restaurants",
                    "View Favourites",
                    "Logout",
                    "Exit"
            );
            int choice = IO.getInt("Enter choice:", 3);

            switch (choice) {
                case 1 -> searchRestaurant();
                case 2 -> browseRestaurants();
                case 3 -> viewFavourites(user);
                case 4 -> {
                    IO.getUserInput("Procedura di logout completata. Premere 'Enter' per tornare al menu principale.");
                    return;
                }
                case 5 -> exit();

                default -> {
                    IO.printErrorMessage("Opzione non valida");
                }
            }
        }
    }


    private static void ownerMenu(User user) {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n|============= The Knife Main menu =============|",
                    "|===================================================|",
                    "Search for a Restaurant", "views owned restaurants", "view latest reviews", "Logout","Exit"
            );
            int choice = IO.getInt("Enter choice:", 3);

            switch (choice) {
                case 1 -> searchRestaurant();
                case 2 -> viewOwnedRestaurants();
                case 2 -> viewOwnerLatestReviews();
                case 4 -> {
                    IO.getUserInput("Procedura di logout completata. Premere 'Enter' per tornare al menu principale.");
                    return;
                }
                case 5 -> exit();

                default -> {
                    IO.printErrorMessage("Opzione non valida");
                }
            }
        }
    }



    private static void viewFavourites(User user) {
        List<Restaurant> favourites = List.copyOf(user.getFavourites());

        if (favourites.isEmpty()) {
            IO.printErrorMessage("You have no favourite restaurants.");
            IO.getUserInput("Press Enter to return.");
            return;
        }

        while (true) {
            IO.clearScreen();
            for (int i = 0; i < favourites.size(); i++) {
                System.out.println("[" + (i + 1) + "] " + favourites.get(i));
            }
            IO.printMenu("", "", "Back");

            int choice = IO.getInt("Select a restaurant to view details or 0 to go back:", favourites.size());
            if (choice == -1) return;
            viewRestaurantDetails(favourites.get(choice));
        }
    }

    public static void viewRestaurantDetails(Restaurant restaurant) {
        while (true) {
            IO.clearScreen();
            System.out.println(restaurant.toString());


            Collection<Review> reviews = restaurant.getReviews().values();
            if (reviews == null || reviews.isEmpty()) {
                System.out.println("No reviews available for this restaurant.\n");
            } else {
                Review best = reviews.stream().max(Comparator.comparingInt(Review::getValue)).orElse(null);
                Review worst = reviews.stream().min(Comparator.comparingInt(Review::getValue)).orElse(null);

                System.out.println("\n★ Best Review:\n" + (best != null ? best.toString() : "N/A"));
                System.out.println("\n☆ Worst Review:\n" + (worst != null ? worst.toString() : "N/A"));
            }

            IO.printMenu("", "", "Back to search");
            int choice = IO.getInt("Enter choice:", 1);
            if (choice == 0) return;
        }
    }
}
