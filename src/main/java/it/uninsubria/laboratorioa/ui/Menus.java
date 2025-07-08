package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.users.User;
import lombok.experimental.UtilityClass;

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

            IO.printMenu("Welcome to the The Knife", "Choose an option:",
                    "Login",
                    "Register",
                    "Search for a Restaurant",
                    "Exit");
            int choice = IO.getInt("Enter choice:", 4);

            switch (choice) {
                case 0 -> System.out.println("WIP");
                case 1 -> System.out.println("WIP 2");
                case 2 -> System.out.println("WIP 3");
                case 3 -> exit();
            }
        /*
        switch (choice) {
            case 0 -> login();
            case 1 -> register();
            case 2 -> searchRestaurant();
            case 3 -> exit();
        }
         */
        }
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


    private static void login() {
        IO.clearScreen();
        String username = IO.getUserInput("Enter username:");
        String password = IO.getUserInput("Enter password:");

        // Placeholder for user authentication logic
        RegisteredUser user = authenticate(username, password);

        if (user != null) {
            IO.printSuccessrMessage("Login successful!");
            IO.getUserInput("Press Enter to continue.");
            userMenu(user);
        } else {
            IO.printErrorMessage("Utente o password errati.");
            IO.getUserInput("Premere enter");
        }
    }

    private static RegisteredUser authenticate(String username, String password) {
        // Replace with actual authentication logic
        return null;
    }

    private static void register() {
        IO.clearScreen();
        IO.getUserInput("[REGISTER] Feature not yet implemented. Press enter to continue.");
    }

    private static void searchRestaurant() {
        IO.clearScreen();
        String name = IO.getUserInput("Enter restaurant name:");
        // Mocked list - Replace with real search logic
        List<Restaurant> found = List.of();

        if (found.isEmpty()) {
            IO.printErrorMessage("No restaurant found with that name.");
            IO.getUserInput("Press Enter to return.");
            return;
        }

        for (int i = 0; i < found.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + found.get(i));
        }

        int selection = IO.getInt("Select restaurant to view details:", found.size());
        viewRestaurantDetails(found.get(selection));
    }

    public static void viewRestaurantDetails(Restaurant restaurant) {
        while (true) {
            IO.clearScreen();
            System.out.println(restaurant.toString());

            List<Review> reviews = restaurant.getReviews();
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

    private static void userMenu(RegisteredUser user) {
        while (true) {
            IO.clearScreen();
            IO.printMenu("User Menu - " + user.getUsername(), "Choose an option:",
                    "Search for a Restaurant",
                    "View Favourites",
                    "Logout");

            int choice = IO.getInt("Enter choice:", 3);

            switch (choice) {
                case 0 -> searchRestaurant();
                case 1 -> viewFavourites(user);
                case 2 -> {
                    IO.getUserInput("Logged out. Press Enter to return to main menu.");
                    return;
                }
            }
        }
    }

    private static void viewFavourites(RegisteredUser user) {
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
}
