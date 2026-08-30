package it.uninsubria.laboratoriob.server;

import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.remote.AuthRemoteImpl;
import it.uninsubria.laboratoriob.server.remote.FavouriteServiceImpl;
import it.uninsubria.laboratoriob.server.remote.RestaurantServiceImpl;
import it.uninsubria.laboratoriob.server.remote.ReviewServiceImpl;
import it.uninsubria.laboratoriob.server.utils.CsvParser;
import it.uninsubria.laboratoriob.server.utils.Database;
import it.uninsubria.laboratoriob.server.utils.HeartbeatServer;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Entry point del server The Knife.
 * <p>
 * Inizializza il database (schema + costanti), avvia il server di heartbeat TCP,
 * carica opazionalmente il dataset Michelin, e registra i servizi RMI
 * ({@code restaurant}, {@code auth}, {@code review}, {@code favourite}) sulla porta 1099.
 *
 * @author Luca Cavagnari
 */
public class TheKnifeServer {

    private final int rmiPort = 1099;
    private final int heartbeatPort = 5555;
    private final long heartbeatIntervalMinutes = 5;

    private final HeartbeatServer tcpHbeatServer;
    private final ServerDataStore dataStore;

    /**
     * Costruisce il server, inizializzando data store, heartbeat, hook di shutdown
     * e lo schema del database.
     */
    public TheKnifeServer() {
        this.dataStore = new ServerDataStore();
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
     * Avvia il server: importa il dataset CSV se richiesto, carica i dati
     * nel data store e registra i servizi RMI.
     *
     * @param args argomenti della riga di comando;
     *             {@code --update [path]} per importare il dataset Michelin
     */
    private void start(String[] args) {
        try {
            if (args.length > 1 && args[0].equals("--update")) {
                CsvParser.updateMichelinDataset(args[1], dataStore);
            } else if (args.length > 0 && args[0].equals("--update")) {
                CsvParser.updateMichelinDataset(null, dataStore);
            }
        } catch (IOException e) {
            System.err.println("Error occured during database update: " + e);
            System.err.println("Shutting down...");
            shutdown();
        }

        dataStore.initialise();

        try {
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind("restaurant", new RestaurantServiceImpl(dataStore));
            registry.rebind("auth", new AuthRemoteImpl(dataStore));
            registry.rebind("review", new ReviewServiceImpl(dataStore));
            registry.rebind("favourite", new FavouriteServiceImpl(dataStore));
            System.out.println("RMI registry created on port " + rmiPort);
        } catch (Exception e) {
            System.err.println("ERROR while creating RMI registry: " + e);
        }
    }

    /**
     * Arresta il server di heartbeat e chiude il connection pool del database.
     */
    private void shutdown() {
        tcpHbeatServer.shutdown();
        Database.shutdown();
    }

    /**
     * Metodo principale. Istanzia e avvia il server.
     *
     * @param args argomenti della riga di comando
     */
    public static void main(String[] args) {
        System.out.println("Loading The Knife Server...");

        TheKnifeServer server = new TheKnifeServer();
        server.start(args);

        System.out.println("Loading completed!");
        System.out.println("Server is running. Connect via the client application.");
    }
}
