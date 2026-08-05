package it.uninsubria.laboratoriob.ui.menus;

import it.uninsubria.laboratoriob.enums.UserRole;
import it.uninsubria.laboratoriob.objects.*;
import it.uninsubria.laboratoriob.ui.IO;
import it.uninsubria.laboratoriob.ui.Login;
import it.uninsubria.laboratoriob.ui.Menus;
import it.uninsubria.laboratoriob.exceptions.AbortOperationException;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

/**
 * Menu per utenti non autenticati (guest).
 * Consente la visualizzazione dei ristoranti e delle recensioni pubbliche.
 */
public class GuestMenus extends Menus {

    public GuestMenus() {
        super(null);
    }

    /**
     * Avvia il menu principale per l'utente ospite.
     */
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
                        User user = Login.login();
                        if (user != null) {
                            Menus menus = (user.getRole().equals(UserRole.OWNER)) ? new OwnerMenus((Owner) user) : new UserMenus((Client) user);
                            menus.openMenu();
                        }
                    } catch (AbortOperationException e) {
                        IO.printErrorMessage(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        User user = Login.register();
                        if (user != null) {
                            Menus menus = (user.getRole().equals(UserRole.OWNER)) ? new OwnerMenus((Owner) user) : new UserMenus((Client) user);
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