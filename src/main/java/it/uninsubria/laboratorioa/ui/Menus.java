package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.enums.UserRole;
import it.uninsubria.laboratorioa.objects.users.Client;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
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

    /**
     * Menu principale per l'accesso iniziale all'applicazione.
     * <p>
     * Permette di effettuare il login, registrarsi, cercare o esplorare ristoranti,
     * oppure uscire dall'applicazione. In base al ruolo seleziona il menu corretto.
     */
    public static void mainMenu() {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌───────────── The Knife Main menu ─────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    "Login",
                    "Register",
                    "Search for a Restaurant",
                    "Browse Restaurants",
                    "Exit"
            );
            int choice = IO.getInt("Enter choice:");

            switch (choice) {
                case 0 -> {
                    try {
                        User user = Login.login();
                        if (user != null) {
                            if (user.getRole().equals(UserRole.OWNER)) ownerMenu((Owner) user);
                            else userMenu((Client) user);
                        }
                    } catch (AbortOperationException e) {
                        IO.printErrorMessage(e.getMessage());
                    }
                }
                case 1 -> {
                    try {
                        User user = Login.register();
                        if (user != null) {
                            if (user.getRole().equals(UserRole.OWNER)) ownerMenu((Owner) user);
                            else userMenu((Client) user);
                        }
                    } catch (AbortOperationException e) {
                        IO.printErrorMessage(e.getMessage());
                    }
                }
                case 2 -> searchRestaurant();
                case 3 -> browseRestaurants();
                case 4 -> exit();
            }
        }
    }

    /**
     * Mostra l'elenco di tutti i ristoranti disponibili da esplorare.
     * <p>
     * Se non sono presenti ristoranti, stampa un messaggio d'errore.
     */
    private static void browseRestaurants() {
        Map<String, Restaurant> list = Loader.getRestaurantsByName();
        if (list.isEmpty()) {
            IO.clearScreen();
            IO.printErrorMessage("Nessun ristorante disponibile al momento.");
            return;
        }

        String[] options = list.keySet().toArray(new String[0]);


        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌─────────────── Sfoglia Ristoranti ────────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    options
            );

            System.out.println("0 - Torna indietro\n");

            int sel = IO.getMenuInt("Seleziona un ristorante da visualizzare ['0' per tornare indietro]:");
            if (sel == 0) return;
            else if (sel <= 0 || sel > list.size()) continue;

            Menus.viewRestaurantDetails(null, list.get(sel - 1));
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

    /**
     * Permette la ricerca di un ristorante per nome.
     * <p>
     * Se trovato, mostra i dettagli del ristorante come utente.
     */
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
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return;
            }
        }

        viewRestaurantDetails(null, r);
    }

    /**
     * Mostra le recensioni ricevute negli ultimi 7 giorni dai ristoranti
     * gestiti dall'owner, ordinate cronologicamente.
     *
     * @param owner proprietario autenticato
     */
    private static void viewOwnerLatestReviews(Owner owner) {
        Map<UUID, Restaurant> restaurants = owner.getRestaurantsById();

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
        System.out.println("┌─── Recensioni ricevute negli ultimi 7 giorni ───┐\n");
        for (Review r : recent) {
            System.out.println(r);
            System.out.println("-".repeat(60));
        }
        IO.getUserInput("\nPremi invio per tornare.");
    }

    /**
     * Mostra e permette di navigare tra i ristoranti registrati dall'owner.
     *
     * @param owner proprietario autenticato
     */
    private static void viewOwnedRestaurants(Owner owner) {
        List<Restaurant> list = new ArrayList<>(owner.getRestaurantsById().values());
        if (list.isEmpty()) {
            IO.clearScreen();
            IO.printErrorMessage("Non hai ristoranti registrati.");
            IO.getUserInput("Premi invio per tornare.");
            return;
        }

        while (true) {
            IO.clearScreen();
            IO.printMenu(
                    "\n┌───────────── Ristoranti di " + owner.getUsername() + " ─────────────┐",
                    "",
                    list.stream().map(Restaurant::getName).toArray(String[]::new)
            );

            System.out.println("│ 0 - Torna indietro\n");
            System.out.println("\n└───────────────────────────────────────────────────┘");

            try {
                int sel = IO.getInt("Seleziona un ristorante da visualizzare:");
                Menus.viewOwnerRestaurantDetails(owner, list.get(sel));
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return;
            }
        }
    }

    /**
     * Menu utente per {@link Client}, con opzioni come preferiti, ricerca e logout.
     *
     * @param user utente autenticato di tipo {@link Client}
     */
    private static void userMenu(Client user) {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌────────────────── The Knife ──────────────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    "Search for a Restaurant",
                    "Browse Restaurants",
                    "View Favourites",
                    "Logout",
                    "Exit"
            );
            int choice = IO.getInt("Enter choice:");

            switch (choice) {
                case 1 -> searchRestaurant();
                case 2 -> browseRestaurants();
                case 3 -> viewFavourites(user);
                case 4 -> {
                    IO.getUserInput("Procedura di logout completata. Premere 'Enter' per tornare al menu principale.");
                    return;
                }
                case 5 -> exit();

                default -> IO.printErrorMessage("Opzione non valida");
            }
        }
    }

    /**
     * Menu proprietario per {@link Owner}, con opzioni specifiche per la gestione dei ristoranti.
     *
     * @param owner proprietario autenticato
     */
    private static void ownerMenu(Owner owner) {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌───────────── The Knife Main menu ─────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    "Search for a Restaurant", "views owned restaurants", "view latest reviews", "Logout", "Exit"
            );
            int choice = IO.getInt("Enter choice:");

            switch (choice) {
                case 1 -> searchRestaurant();
                case 2 -> viewOwnedRestaurants(owner);
                case 3 -> viewOwnerLatestReviews(owner);
                case 4 -> {
                    IO.getUserInput("Procedura di logout completata. Premere 'Enter' per tornare al menu principale.");
                    return;
                }
                case 5 -> exit();

                default -> IO.printErrorMessage("Opzione non valida");
            }
        }
    }

    /**
     * Visualizza i ristoranti preferiti dell'utente {@link Client}.
     *
     * @param client utente autenticato
     */
    private static void viewFavourites(Client client) {
        Set<UUID> favourites = client.getFavouriteRestourants();

        if (favourites == null || favourites.isEmpty()) {
            IO.printErrorMessage("You have no favourite restaurants.");
            IO.getUserInput("Press Enter to return.");
            return;
        }

        List<Restaurant> restaurants = Loader.getRestaurantsById().entrySet().stream()
                .filter(e -> favourites.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "",
                    "",
                    restaurants.stream().map(Restaurant::getName).toArray(String[]::new)
            );

            int choice = IO.getInt("Seleziona un ristorante da visionare");
            if (choice <= 0 || choice > restaurants.size()) continue;

            viewRestaurantDetails(client, restaurants.get(choice - 1));
        }
    }

    /**
     * Mostra e consente la modifica dei dettagli di un ristorante da parte del proprietario.
     *
     * @param owner      proprietario autenticato
     * @param restaurant ristorante da modificare
     */
    public static void viewOwnerRestaurantDetails(Owner owner, Restaurant restaurant) {
        while (true) {
            IO.clearScreen();
            System.out.println(restaurant);

            Collection<Review> reviews = restaurant.getReviews().values();
            if (reviews == null || reviews.isEmpty()) {
                System.out.println("No reviews available for this restaurant.\n");
            } else {
                Review best = reviews.stream().max(Comparator.comparingInt(Review::getValue)).orElse(null);
                Review worst = reviews.stream().min(Comparator.comparingInt(Review::getValue)).orElse(null);

                System.out.println("\n★ Best Review:\n" + (best != null ? best : "N/A"));
                System.out.println("\n☆ Worst Review:\n" + (worst != null ? worst : "N/A"));
            }


            IO.printMenu(
                    "┌─────Operazioni disponibili:─────┐",
                    "└──────────────────────────────────┘",
                    "Modifica Nome",
                    "Modifica Descrizione",
                    "Modifica Website",
                    "Modifica Telefono",
                    "Modifica Location",
                    "Modifica Fascia di Prezzo",
                    "Modifica Premi",
                    "Modifica Stella Verde",
                    "Modifica Delivery",
                    "Modifica Prenotazione Online",
                    "Modifica Tipi di Cucina",
                    "Modifica Servizi",
                    "Torna indietro"
            );

            int choice = IO.getInt("Seleziona un'opzione:");

            try {
                switch (choice) {
                    case 1 -> restaurant.setName(IO.getUserInput("Nuovo nome:"));
                    case 2 -> restaurant.setDescription(IO.getUserInput("Nuova descrizione:"));
                    case 3 -> restaurant.setWebsiteUrl(IO.getUserInput("Nuovo sito web:"));
                    case 4 -> restaurant.setPhone(IO.getUserInput("Nuovo numero di telefono:"));
                    case 5 -> {
                        Location location = IO.getLocationInput(false);
                        IO.validateLocation(location);

                        restaurant.setLocation(location);
                    }
                    case 6 -> {
                        PriceRange range = IO.getEnumInput(PriceRange.class, "Inserisci nuova fascia di prezzo");
                        restaurant.setPriceRange(range);
                    }
                    case 7 -> {
                        Award award = IO.getEnumInput(Award.class, "Nuovo premio: (NONE, MICHELIN_STAR, BIB_GOURMAND, ...)");
                        restaurant.setAward(award);
                    }

                    case 8 -> restaurant.setGreenStar(IO.getBooleanInput("Ha la Stella Verde?"));
                    case 9 -> restaurant.setHasDelivery(IO.getBooleanInput("Offre consegna a domicilio?"));
                    case 10 -> restaurant.setHasOnlineBooking(IO.getBooleanInput("Offre prenotazione online?"));
                    case 11 -> {
                        Set<CuisineType> cuisines = IO.getEnumSetInput(CuisineType.class, "Inserisci tipi di cucina:");
                        restaurant.getCuisinesTypes().addAll(cuisines);
                    }
                    case 12 -> {
                        Set<String> services = IO.parseValidatedStrings("Inserisci servizi:");
                        restaurant.getServices().addAll(services);
                    }
                    case 13 -> {
                        restaurant.build(); // Ricostruisci jsonObject aggiornato
                        return;
                    }
                    default -> IO.printErrorMessage("Opzione non valida.");
                }
            } catch (IllegalArgumentException e) {
                IO.printErrorMessage(e.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage("Abort: " + e.getMessage());
                break;
            }

            IO.getUserInput("Premi invio per continuare.");


        }
    }

    /**
     * Mostra i dettagli di un ristorante a un utente {@link Client},
     * permettendo di visualizzare, creare o modificare recensioni.
     *
     * @param user       utente autenticato (può essere null)
     * @param restaurant ristorante da visualizzare
     */
    public static void viewRestaurantDetails(Client user, Restaurant restaurant) {
        while (true) {
            IO.clearScreen();
            System.out.println(restaurant);

            Map<UUID, Review> reviews = restaurant.getReviews();

            System.out.println("┌─────────────── ★Recensioni★ ────────────────┐\n");
            if (reviews == null || reviews.isEmpty()) {

                System.out.println("│ No reviews available for this restaurant.");
                System.out.println("└───────────────────────────────────────────┘");
            } else {
                Review best = reviews.values().stream().max(Comparator.comparingInt(Review::getValue)).orElse(null);
                Review worst = reviews.values().stream().min(Comparator.comparingInt(Review::getValue)).orElse(null);

                System.out.println("\n│ ★ Migliore:\n" + best);
                System.out.println("\n│ ☆ Peggiore:\n" + worst);
                System.out.println("└───────────────────────────────────────────┘");
            }

            Review usersReview = null;
            if (user != null && reviews != null && reviews.get(user.getId()) != null) {
                System.out.println("┌─────────────── La Tua Recensione ────────────────┐\n");
                usersReview = reviews.get(user.getId());
                System.out.println();
            }


            String[] options = new String[1];
            if (usersReview != null) options = new String[]{
                    "Aggiorna testo recensione", "Aggiorna valore recensione",
                    "Cancella recensione", "Torna indietro"};

            else options = new String[]{
                    "Aggiungi recensione", "Torna indietro"
            };


            IO.printMenu(
                    "─────Operazioni disponibili:─────",
                    "──────────────────────────────────",
                    options
            );

            int choice = IO.getInt("Seleziona un'opzione:");

            try {
                switch (choice) {
                    case 1 -> {
                        if (usersReview == null) {
                            int value = IO.getInt("Che voto daresti al Ristorante '" + restaurant.getName() + "' ? [1-5]");
                            String text = IO.getUserInput("Raccontaci della tua esperienza [4-200 caratteri]:");

                            Review review = new Review(restaurant, user, value, text);
                            break;
                        }

                        String newText = IO.getUserInput("Raccontaci della tua esperienza [4-200 caratteri]:");
                        IO.validateString(newText);
                        usersReview.setText(newText);
                    }
                    default -> IO.printErrorMessage("Opzione non valida.");
                }
            } catch (IllegalArgumentException e) {
                IO.printErrorMessage(e.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage("Abort: " + e.getMessage());
                break;
            }

            IO.getUserInput("Premi invio per continuare.");


        }
    }


    /**
     * Consente all'owner di creare un nuovo ristorante,
     * raccogliendo i dati tramite input guidato.
     *
     * @param owner proprietario autenticato
     */
    private static void createRestaurant(Owner owner) {
        try {
            IO.clearScreen();
            System.out.println("─── Crea un nuovo ristorante ───");

            String name = null;
            while (name == null) {
                try {
                    name = IO.getUserInput("Nome del ristorante:");
                    IO.validateString(name);
                    if (Loader.getRestaurantsByName().containsKey(name))
                        throw new IllegalArgumentException("Nome già in uso.");
                } catch (IllegalArgumentException | AbortOperationException e) {
                    IO.printErrorMessage(e.getMessage());
                    name = null;
                }
            }

            String description = IO.getUserInput("Descrizione:");
            String website = IO.getUserInput("Sito web:");
            String phone = IO.getPhoneNumberInput();

            // Location
            Location location = IO.getLocationInput(false);

            PriceRange priceRange = IO.getEnumInput(PriceRange.class, "Fascia di prezzo:");

            boolean hasDelivery = IO.getBooleanInput("Offre consegna a domicilio? [s/n]:");
            boolean hasBooking = IO.getBooleanInput("Offre prenotazione online? [s/n]:");
            Award award = IO.getEnumInput(Award.class, "Premio assegnato:");
            boolean greenStar = IO.getBooleanInput("Possiede Stella Verde Michelin? [s/n]:");

            // Cuisines
            Set<CuisineType> cuisines = IO.getEnumSetInput(CuisineType.class, "Seleziona tipi di cucina.");


            // Services
            Set<String> services = IO.parseValidatedStrings("Inserisci servizi offerti (es. 'delivery', 'wifi')");


            // Costruzione e registrazione
            try {
                Restaurant r = new Restaurant(name, description, website, owner, phone,
                        location, priceRange, hasDelivery, hasBooking, award,
                        greenStar, cuisines, services);

                Loader.getRestaurantsById().put(r.getId(), r);
                Loader.getRestaurantsByName().put(name, r);

                r.save();

            } catch (IllegalArgumentException e) {
                IO.printErrorMessage(e.getMessage());
                IO.getUserInput("ritornando al menu principale");
                return;
            }

            IO.printSuccessrMessage("Ristorante creato con successo.");
            IO.getUserInput("Premi invio per tornare al menu.");
        } catch (AbortOperationException e) {
            IO.printErrorMessage("Abort: " + e.getMessage());
        }
    }

}
