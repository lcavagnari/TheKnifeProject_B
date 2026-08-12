package it.uninsubria.laboratoriob.server;

import it.uninsubria.laboratoriob.server.ui.IO;
import it.uninsubria.laboratoriob.server.ui.menus.GuestMenus;
import it.uninsubria.laboratoriob.server.utils.Database;
import it.uninsubria.laboratoriob.server.utils.HeartbeatServer;
import it.uninsubria.laboratoriob.server.utils.Loader;

import java.io.IOException;

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
public class TheKnifeServer {

    private final int rmiPort = 1099;
    private final int heartbeatPort = 5555;
    private final long heartbeatIntervalMinutes = 5;

    private final HeartbeatServer tcpHbeatServer;

    public TheKnifeServer() {
        this.tcpHbeatServer = new HeartbeatServer(heartbeatPort, heartbeatIntervalMinutes);

        tcpHbeatServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(tcpHbeatServer::shutdown));

        if (!Database.initTables())
            IO.printErrorMessage("ERROR while initialising database schema");

        if (!Database.initialiseConstants())
            IO.printErrorMessage("ERROR while initialising database constants");
    }

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
    private void start(String[] args) {
        try {
            if (args.length > 1 && args[0].equals("--update")) {
                Loader.updateMichelinDataset(args[1]);

            } else if (args.length > 0 && args[0].equals("--update")) {
                Loader.updateMichelinDataset(null);
            }
        } catch (IOException e) {
            IO.printErrorMessage("Error occured during database update: "+e);
            IO.printErrorMessage("Shutting down...");
            shutdown();
        }


        Loader.initialiseMaps();
    }

    private void shutdown() {
        tcpHbeatServer.shutdown();
        Database.shutdown();
    }


    public static void main(String[] args) {
        IO.printSuccessMessage("Loading The Knife...");

        TheKnifeServer server = new TheKnifeServer();
        server.start(args);

        IO.printSuccessMessage("Loading completed!");
        new GuestMenus().openMenu();
    }
}
