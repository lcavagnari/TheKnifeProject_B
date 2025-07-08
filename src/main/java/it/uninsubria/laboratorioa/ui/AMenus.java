package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.Loader;

import java.util.Map;

public abstract class AMenus {
    protected final User user;

    public AMenus(User user) {
        this.user = user;
    }

    protected void searchRestaurant() {
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

        viewUserRestaurantDetails(r);
    }

    protected void browseRestaurants() {
        Map<String, Restaurant> restaurants = Loader.getRestaurantsByName();
        if (restaurants.isEmpty()) {
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

    protected void exit() {
        IO.clearScreen();
        System.out.println("Goodbye!");

        IO.closeScanner();
        System.exit(0);
    }

    public abstract void openMenu();

    protected abstract void viewUserRestaurantDetails(Restaurant restaurant);
}
