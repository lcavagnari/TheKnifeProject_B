package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.Loader;

import java.util.Map;

/**
 * Classe astratta di base per i menu testuali dell'applicazione.
 * <p>
 * Ogni menu rappresenta una sezione dell'interfaccia console associata a un utente specifico.
 * Le sottoclassi concrete (ad esempio menu cliente o menu gestore) estendono questa classe
 * e implementano la logica personalizzata di navigazione e visualizzazione.
 * </p>
 *
 * <h2>Responsabilità principali</h2>
 * <ul>
 *   <li>Gestione delle operazioni comuni di navigazione (ricerca ristoranti, esplorazione, uscita).</li>
 *   <li>Definizione dei metodi astratti per l’apertura del menu e la visualizzazione dei dettagli.</li>
 * </ul>
 *
 * <p>Tutti i metodi di I/O interagiscono con la console tramite {@link IO}.</p>
 *
 * @see IO
 * @see Loader
 * @see it.uninsubria.laboratorioa.objects.Restaurant
 * @see it.uninsubria.laboratorioa.objects.users.User
 */
public abstract class Menus {

    /** Utente attualmente autenticato che utilizza il menu. */
    protected final User user;

    /**
     * Costruttore base del menu.
     *
     * @param user istanza {@link User} associata al menu corrente;
     *             rappresenta l'utente loggato o attivo nel contesto.
     */
    public Menus(User user) {
        this.user = user;
    }

    /**
     * Avvia una ricerca di un ristorante per nome.
     * <p>
     * Il metodo richiede un input testuale all’utente, convalidato tramite {@link IO#validateString(String)}.
     * Se il ristorante esiste in {@link Loader#getRestaurantsByName()}, ne visualizza i dettagli
     * tramite {@link #viewRestaurantDetails(Restaurant)}. In caso di errore o annullamento,
     * viene mostrato un messaggio e il metodo termina senza eccezioni propagate.
     * </p>
     *
     * @throws AbortOperationException gestita internamente se l’utente annulla con il comando previsto
     */
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
                IO.printErrorMessage(e.getMessage() +
                        ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return;
            }
        }

        viewRestaurantDetails(r);
    }

    /**
     * Mostra un elenco di tutti i ristoranti disponibili nel sistema.
     * <p>
     * Recupera i ristoranti dalla mappa {@link Loader#getRestaurantsByName()} e li stampa
     * in forma di menu testuale tramite {@link IO#printMenu(String, String, String...)}.
     * </p>
     *
     * <p>Se non sono presenti ristoranti, viene mostrato un messaggio di errore.</p>
     */
    protected void browseRestaurants() {
        Map<String, Restaurant> restaurants = Loader.getRestaurantsByName();
        if (restaurants.isEmpty()) {
            IO.printErrorMessage("Nessun ristorante disponibile al momento.");
            return;
        }

        String[] options = restaurants.keySet().toArray(new String[0]);

        IO.printMenu(
                "\n|============= Esplora Ristoranti =============|",
                "|===================================================|",
                options
        );
    }

    /**
     * Termina il programma in modo controllato.
     * <p>
     * Pulisce la console, mostra un messaggio di chiusura e chiude lo {@link java.util.Scanner}
     * condiviso tramite {@link IO#closeScanner()}. Infine invoca {@link System#exit(int)} con codice 0.
     * </p>
     */
    protected void exit() {
        IO.clearScreen();
        System.out.println("Goodbye!");
        IO.closeScanner();
        System.exit(0);
    }

    /**
     * Metodo astratto che deve essere implementato dalle sottoclassi per aprire il menu principale
     * dell'utente corrente (es. menu cliente o menu gestore).
     * <p>Può includere cicli di navigazione e chiamate ad altri sottomenu.</p>
     */
    public abstract void openMenu();

    /**
     * Metodo astratto per visualizzare i dettagli di un ristorante specifico.
     * <p>
     * Ogni sottoclasse definisce il formato di visualizzazione (es. recensioni,
     * dettagli contatto, opzioni di modifica).
     * </p>
     *
     * @param restaurant ristorante di cui visualizzare le informazioni
     */
    protected abstract void viewRestaurantDetails(Restaurant restaurant);
}
