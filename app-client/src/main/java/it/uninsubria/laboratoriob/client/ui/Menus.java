package it.uninsubria.laboratoriob.client.ui;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.ResturantServiceInter;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

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
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ResturantServiceInter service = (ResturantServiceInter) registry.lookup("restaurant");

            long total = service.count();
            if (total == 0) {
                IO.printErrorMessage("Nessun ristorante disponibile al momento.");
                return;
            }

            int pageSize = 20;
            int offset = 0;

            while (true) {
                List<Restaurant> page = service.findAll(offset, pageSize);

                IO.clearScreen();
                System.out.println("|============= Esplora Ristoranti =============|");
                System.out.println("| Pagina " + ((offset / pageSize) + 1) + " di " + ((int) Math.ceil((double) total / pageSize)) + " | Totale: " + total + " ristoranti\n");

                for (int i = 0; i < page.size(); i++)
                    System.out.println("│ " + (i + 1) + " - " + page.get(i).getName());

                boolean hasPrev = offset > 0;
                boolean hasNext = offset + pageSize < total;

                System.out.println();
                if (hasPrev) System.out.println("│ P - Pagina precedente");
                if (hasNext) System.out.println("│ N - Pagina successiva");
                System.out.println("│ 0 - Torna indietro");
                System.out.println("\n|===================================================|");

                String input = IO.getUserInput("Seleziona un'opzione:");
                if (input.equalsIgnoreCase("0")) return;

                if (hasPrev && input.equalsIgnoreCase("P")) {
                    offset = Math.max(0, offset - pageSize);
                } else if (hasNext && input.equalsIgnoreCase("N")) {
                    offset += pageSize;
                } else {
                    try {
                        int choice = Integer.parseInt(input);
                        if (choice >= 1 && choice <= page.size()) {
                            viewRestaurantDetails(page.get(choice - 1));
                        } else {
                            IO.printErrorMessage("Opzione non valida");
                        }
                    } catch (NumberFormatException e) {
                        IO.printErrorMessage("Opzione non valida");
                    }
                }
            }
        } catch (AbortOperationException e) {
            IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
        } catch (Exception e) {
            IO.printErrorMessage("Errore di connessione al server: " + e.getMessage());
        }
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
