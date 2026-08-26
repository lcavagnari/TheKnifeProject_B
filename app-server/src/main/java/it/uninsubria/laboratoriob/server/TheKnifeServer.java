package it.uninsubria.laboratoriob.server;

import it.uninsubria.laboratoriob.server.data.remote.AuthRemoteImpl;
import it.uninsubria.laboratoriob.server.data.remote.FavouriteServiceImpl;
import it.uninsubria.laboratoriob.server.data.remote.RestaurantServiceImpl;
import it.uninsubria.laboratoriob.server.data.remote.ReviewServiceImpl;
import it.uninsubria.laboratoriob.server.utils.Database;
import it.uninsubria.laboratoriob.server.utils.HeartbeatServer;
import it.uninsubria.laboratoriob.server.utils.Loader;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Classe principale di ingresso dell'applicazione The Knife.
 * <p>
 * Responsabile dell'avvio dell'applicazione e dell'inizializzazione del database.
 * <p>
 * Supporta la modalità di aggiornamento: esegue il parsing del dataset Michelin CSV
 * e popola il database (con {@code --update} opzionalmente seguito dal percorso del file).
 * </p>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see Loader
 * @see Database

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
        Runtime.getRuntime().addShutdownHook(new Thread(Database::shutdown));

        if (!Database.initTables())
            System.err.println("ERROR while initialising database schema");

        if (!Database.initialiseConstants())
            System.err.println("ERROR while initialising database constants");
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
     *               <li>nessun argomento - avvia il server</li>
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
            System.err.println("Error occured during database update: " + e);
            System.err.println("Shutting down...");
            shutdown();
        }


        Loader.initialiseMaps();

        try {
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind("restaurant", new RestaurantServiceImpl());
            registry.rebind("auth", new AuthRemoteImpl());
            registry.rebind("review", new ReviewServiceImpl());
            registry.rebind("favourite", new FavouriteServiceImpl());
            System.out.println("RMI registry created on port " + rmiPort);
        } catch (Exception e) {
            System.err.println("ERROR while creating RMI registry: " + e);
        }
    }

    private void shutdown() {
        tcpHbeatServer.shutdown();
        Database.shutdown();
    }


    public static void main(String[] args) {
        System.out.println("Loading The Knife Server...");

        TheKnifeServer server = new TheKnifeServer();
        server.start(args);

        System.out.println("Loading completed!");
        System.out.println("Server is running. Connect via the client application.");
    }
}
