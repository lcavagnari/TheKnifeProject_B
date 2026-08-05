package it.uninsubria.laboratoriob.ui.menus;


import it.uninsubria.laboratoriob.Validators;
import it.uninsubria.laboratoriob.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.objects.Customer;
import it.uninsubria.laboratoriob.objects.Restaurant;
import it.uninsubria.laboratoriob.objects.Review;
import it.uninsubria.laboratoriob.ui.IO;
import it.uninsubria.laboratoriob.ui.Menus;
import it.uninsubria.laboratoriob.utils.Loader;

import java.util.*;

/**
 * Classe che gestisce i menu e le operazioni disponibili per gli utenti di tipo {@link Customer}.<p>
 * Estende {@link Menus} fornendo funzionalità specifiche per i clienti.<p>
 * Permette di cercare ristoranti, visualizzare preferiti, creare e gestire recensioni.<p>
 * <p>
 * Autore: Luke
 *
 * @version 1.0
 */
public class CustomerMenus extends Menus {

    /**
     * Riferimento al client correntemente autenticato.<p>
     * Utilizzato per accedere ai ristoranti preferiti e alle recensioni personali.
     */
    private final Customer customer;

    /**
     * Costruttore che inizializza il menu per un client specifico.<p>
     * Invoca il costruttore della superclasse passando il client come utente base.
     *
     * @param customer il cliente per cui creare il menu
     */
    public CustomerMenus(Customer customer) {
        super(customer);
        this.customer = customer;
    }

    /**
     * Apre il menu principale per il client.<p>
     * Presenta le opzioni disponibili e gestisce la navigazione tra le diverse funzionalità.<p>
     * Il ciclo continua finché l'utente non sceglie di disconnettersi o uscire dall'applicazione.
     */
    @Override
    public void openMenu() {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌────────────────── The Knife ──────────────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    "Cerca un Ristorante",
                    "Esplora Ristoranti",
                    "Mostra Preferiti",
                    "Disconnetti",
                    "Esci"
            );
            int choice = IO.getInt("Enter choice:");

            switch (choice) {
                case 1 -> searchRestaurant();
                case 2 -> browseRestaurants();
                case 3 -> viewFavourites();
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
     * Visualizza i ristoranti preferiti dell'utente {@link Customer}.<p>
     * Recupera gli UUID dei ristoranti preferiti e li carica dal {@link Loader}.<p>
     * Mostra un menu interattivo per selezionare un ristorante e visualizzarne i dettagli.<p>
     * Se non ci sono preferiti, viene mostrato un messaggio informativo.
     */
    private void viewFavourites() {
        Set<UUID> favourites = customer.getFavouriteRestourants();

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

            viewRestaurantDetails(restaurants.get(choice - 1));
        }
    }

    /**
     * Mostra i dettagli di un ristorante a un utente {@link Customer}.<p>
     * Visualizza le informazioni complete del ristorante, incluse le recensioni migliori e peggiori.<p>
     * Permette al client di:<p>
     * - Creare una nuova recensione se non ne ha ancora scritta una<p>
     * - Modificare il testo della propria recensione esistente<p>
     * - Modificare il voto della propria recensione esistente<p>
     * - Cancellare la propria recensione<p>
     * <p>
     * Le operazioni vengono gestite con validazione e messaggi di errore appropriati.
     *
     * @param restaurant ristorante da visualizzare e per cui gestire le recensioni
     */
    @Override
    public void viewRestaurantDetails(Restaurant restaurant) {
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
            if (customer != null && reviews != null && reviews.get(customer.getId()) != null) {
                System.out.println("┌─────────────── La Tua Recensione ────────────────┐\n");
                usersReview = reviews.get(customer.getId());
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

                            Review review = new Review(restaurant, customer, value, text);
                            break;
                        }

                        String newText = IO.getUserInput("Raccontaci della tua esperienza [4-200 caratteri]:");
                        Validators.validateString(newText);
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
}