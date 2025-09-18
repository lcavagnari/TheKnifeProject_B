package it.uninsubria.laboratorioa.ui.menus;


import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.users.Client;
import it.uninsubria.laboratorioa.ui.IO;
import it.uninsubria.laboratorioa.ui.Menus;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.Loader;

import java.util.*;

public class UserMenus extends Menus {

    private final Client client;

    public UserMenus(Client client) {
        super(client);
        this.client = client;
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

    /**
     * Visualizza i ristoranti preferiti dell'utente {@link Client}.
     */
    private void viewFavourites() {
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

            viewRestaurantDetails(restaurants.get(choice - 1));
        }
    }

    /**
     * Mostra i dettagli di un ristorante a un utente {@link Client},
     * permettendo di visualizzare, creare o modificare recensioni.
     *
     * @param restaurant ristorante da visualizzare
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
            if (client != null && reviews != null && reviews.get(client.getId()) != null) {
                System.out.println("┌─────────────── La Tua Recensione ────────────────┐\n");
                usersReview = reviews.get(client.getId());
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

                            Review review = new Review(restaurant, client, value, text);
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
}
