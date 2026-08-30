package it.uninsubria.laboratoriob.client.cli;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.exceptions.ServiceUnavailableException;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;

import java.util.List;

/**
 * Classe astratta di base per i menu testuali del client.
 * Fornisce operazioni comuni di navigazione (ricerca ristoranti, esplorazione, uscita).
 */
public abstract class Menus {

    /** Utente attualmente autenticato. */
    protected final User user;
    /** Facade dei dati client. */
    protected final ClientDataStore dataStore;

    /**
     * Costruisce il menu base con utente e facade dati.
     *
     * @param user utente autenticato
     * @param dataStore facade dei dati client
     */
    public Menus(User user, ClientDataStore dataStore) {
        this.user = user;
        this.dataStore = dataStore;
    }

    /** Cerca un ristorante per nome e mostra i dettagli. */
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
                    throw new IllegalArgumentException("Nessun ristorante trovato con nome " + name + ", riprovare");

            } catch (IllegalArgumentException ex) {
                IO.printErrorMessage(ex.getMessage());

            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() +
                        ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return;

            } catch (ServiceUnavailableException e) {
                IO.printErrorMessage("Server non disponibile. Riprova più tardi.");
                return;
            }
        }

        viewRestaurantDetails(r);
    }

    /** Mostra la lista paginata di tutti i ristoranti disponibili. */
    protected void browseRestaurants() {
        try {
            long total = dataStore.getRestaurantDAO().countRemote();
            if (total == 0) {
                IO.printErrorMessage("Nessun ristorante disponibile al momento.");
                return;
            }

            int pageSize = 20;
            int offset = 0;

            while (true) {
                List<Restaurant> page = dataStore.getRestaurantDAO().findAll(offset, pageSize);

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

                if (hasPrev && input.equalsIgnoreCase("P"))
                    offset = Math.max(0, offset - pageSize);
                else if (hasNext && input.equalsIgnoreCase("N"))
                    offset += pageSize;
                else {
                    try {
                        int choice = Integer.parseInt(input);
                        if (choice >= 1 && choice <= page.size())
                            viewRestaurantDetails(page.get(choice - 1));
                        else
                            IO.printErrorMessage("Opzione non valida");
                    } catch (NumberFormatException e) {
                        IO.printErrorMessage("Opzione non valida");
                    }
                }
            }
        } catch (AbortOperationException e) {
            IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
        } catch (ServiceUnavailableException e) {
            IO.printErrorMessage("Server non disponibile. Riprova più tardi.");
        } catch (Exception e) {
            IO.printErrorMessage("Errore di connessione al server: " + e.getMessage());
        }
    }

    /** Chiude l'applicazione con un messaggio di saluto. */
    protected void exit() {
        IO.clearScreen();
        System.out.println("Goodbye!");
        IO.closeScanner();
        System.exit(0);
    }

    /**
     * Apre il menu principale del ruolo corrente, gestendo l'interazione utente in un ciclo continuo.
     */
    public abstract void openMenu();

    /**
     * Mostra i dettagli e le recensioni del ristorante selezionato.
     *
     * @param restaurant il ristorante da visualizzare
     */
    protected abstract void viewRestaurantDetails(Restaurant restaurant);
}
