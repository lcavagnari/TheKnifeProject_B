package it.uninsubria.laboratoriob.client.ui;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;

/**
 * Classe astratta di base per i menu testuali del client.
 * Fornisce operazioni comuni di navigazione (ricerca ristoranti, esplorazione, uscita).
 */
public abstract class Menus {

    protected final User user;
    protected final ClientDataStore dataStore;

    public Menus(User user, ClientDataStore dataStore) {
        this.user = user;
        this.dataStore = dataStore;
    }

    protected void searchRestaurant() {
        IO.clearScreen();

        Restaurant r = null;
        while (r == null) {
            try {
                String name = IO.getUserInput("Enter restaurant name:");
                Validators.validateString(name);

                r = dataStore.getRestaurantDAO().findAll().stream()
                        .filter(rest -> rest.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .orElse(null);

                if (r == null)
                    throw new IllegalArgumentException("Nessun ristorante trovato, riprovare");

            } catch (IllegalArgumentException ex) {
                IO.printErrorMessage(ex.getMessage());

            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() +
                        ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return;
            }
        }

        viewRestaurantDetails(r);
    }

    protected void browseRestaurants() {
        var restaurants = dataStore.getRestaurantDAO().findAll();
        if (restaurants.isEmpty()) {
            IO.printErrorMessage("Nessun ristorante disponibile al momento.");
            return;
        }

        String[] options = restaurants.stream()
                .map(Restaurant::getName)
                .toArray(String[]::new);

        IO.printMenu(
                "\n|============= Esplora Ristoranti =============|",
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

    protected abstract void viewRestaurantDetails(Restaurant restaurant);
}
