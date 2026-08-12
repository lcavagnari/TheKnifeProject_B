package it.uninsubria.laboratoriob;

import it.uninsubria.laboratoriob.ui.IO;
import it.uninsubria.laboratoriob.ui.menus.GuestMenus;
import it.uninsubria.laboratoriob.utils.Database;
import it.uninsubria.laboratoriob.utils.Loader;

/**
 * Classe principale di ingresso dell'applicazione The Knife.
 * <p>
 * Responsabile dell'avvio dell'applicazione, dell'inizializzazione del database
 * e della navigazione del menu principale. Supporta due modalità di esecuzione:
 * </p>
 * <ul>
 *   <li><b>Modalità aggiornamento</b>: esegue il parsing del dataset Michelin CSV
 *       e popola il database (con {@code --update} opzionalmente seguito dal percorso del file).</li>
 *   <li><b>Modalità interattiva</b>: avvia il menu CLI per la navigazione e gestione dei ristoranti.</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see Loader
 * @see Database
 * @see GuestMenus
 */
public class TheKnife {

    /**
     * Metodo principale di ingresso dell'applicazione.
     * <p>
     * Inizializza le tabelle del database e i dati costanti, poi gestisce
     * la modalità di esecuzione in base agli argomenti della riga di comando.
     * </p>
     *
     * @param args argomenti della riga di comando:
     *             <ul>
     *               <li>{@code --update [path]} - aggiorna il dataset Michelin</li>
     *               <li>nessun argomento - avvia il menu interattivo</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (!Database.initTables())
            IO.printErrorMessage("ERROR while initialising database schema");

        if (!Database.initialiseConstants())
            IO.printErrorMessage("ERROR while initialising database constants");

        if (args.length > 1 && args[0].equals("--update")) {
            Loader.updateMichelinDataset(args[1]);
            return;

        } else if (args.length > 0 && args[0].equals("--update")) {
            Loader.updateMichelinDataset(null);
            return;
        }

        IO.printSuccessMessage("Loading The Knife...");


        Loader.initialiseMaps();
        new GuestMenus().openMenu();
        Database.shutdown();
    }
}
