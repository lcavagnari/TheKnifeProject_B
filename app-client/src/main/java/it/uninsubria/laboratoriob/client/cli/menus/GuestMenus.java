package it.uninsubria.laboratoriob.client.cli.menus;

import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.cli.IO;
import it.uninsubria.laboratoriob.client.cli.Menus;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

/**
 * Menu per utenti non autenticati (guest).
 */
public class GuestMenus extends Menus {

    public GuestMenus(ClientDataStore dataStore) {
        super(null, dataStore);
    }

    @Override
    public void openMenu() {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌───────────── The Knife Main menu ─────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    "Login",
                    "Register",
                    "Cerca un Ristorante",
                    "Esplora Ristoranti",
                    "Esci"
            );
            int choice = IO.getInt("Enter choice:");

            switch (choice) {
                case 1 -> {
                    try {
                        User user = LoginMenu.login(dataStore);
                        if (user != null) {
                            Menus menus = (user.getRole().equals(UserRole.OWNER))
                                    ? new OwnerMenus((Owner) user, dataStore)
                                    : new CustomerMenus((Customer) user, dataStore);
                            menus.openMenu();
                        }
                    } catch (AbortOperationException e) {
                        IO.printErrorMessage(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        User user = LoginMenu.register(dataStore);
                        if (user != null) {
                            Menus menus = (user.getRole().equals(UserRole.OWNER))
                                    ? new OwnerMenus((Owner) user, dataStore)
                                    : new CustomerMenus((Customer) user, dataStore);
                            menus.openMenu();
                        }
                    } catch (AbortOperationException e) {
                        IO.printErrorMessage(e.getMessage());
                    }
                }
                case 3 -> searchRestaurant();
                case 4 -> browseRestaurants();
                case 5 -> exit();
            }
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

            IO.printMenu(
                    "─────Operazioni disponibili:─────",
                    "──────────────────────────────────",
                    "Torna indietro"
            );

            int choice = IO.getInt("Seleziona un'opzione:");

            try {
                if (choice == 1) return;
                else IO.printErrorMessage("Opzione non valida.");

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
