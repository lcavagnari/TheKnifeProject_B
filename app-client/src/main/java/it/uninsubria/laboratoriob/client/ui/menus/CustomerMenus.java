package it.uninsubria.laboratoriob.client.ui.menus;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.ui.Menus;

import java.util.*;

/**
 * Menu per utenti di tipo Customer.
 */
public class CustomerMenus extends Menus {

    private final Customer customer;

    public CustomerMenus(Customer customer, ClientDataStore dataStore) {
        super(customer, dataStore);
        this.customer = customer;
    }

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

    private void viewFavourites() {
        Set<UUID> favourites = customer.getFavouriteRestourants();

        if (favourites == null || favourites.isEmpty()) {
            IO.printErrorMessage("You have no favourite restaurants.");
            IO.getUserInput("Press Enter to return.");
            return;
        }

        List<Restaurant> restaurants = dataStore.getRestaurantDAO().findAll().stream()
                .filter(r -> favourites.contains(r.getId()))
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
                            restaurant.addReview(review);
                            dataStore.getReviewDAO().save(review);
                            IO.printSuccessMessage("Recensione salvata.");
                            break;
                        }

                        String newText = IO.getUserInput("Raccontaci della tua esperienza [4-200 caratteri]:");
                        Validators.validateString(newText);
                        usersReview.setText(newText);
                        dataStore.getReviewDAO().update(usersReview);
                        IO.printSuccessMessage("Recensione aggiornata.");
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
