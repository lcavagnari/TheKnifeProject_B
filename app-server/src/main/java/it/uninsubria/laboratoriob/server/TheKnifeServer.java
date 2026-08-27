package it.uninsubria.laboratoriob.server;

import it.uninsubria.laboratoriob.server.data.ServerDataStore;
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

public class TheKnifeServer {

    private final int rmiPort = 1099;
    private final int heartbeatPort = 5555;
    private final long heartbeatIntervalMinutes = 5;

    private final HeartbeatServer tcpHbeatServer;
    private final ServerDataStore dataStore;
    private final Loader loader;

    public TheKnifeServer() {
        this.dataStore = new ServerDataStore();
        this.loader = new Loader(dataStore);
        this.tcpHbeatServer = new HeartbeatServer(heartbeatPort, heartbeatIntervalMinutes);

        tcpHbeatServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(tcpHbeatServer::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(Database::shutdown));

        if (!Database.initTables())
            System.err.println("ERROR while initialising database schema");

        if (!Database.initialiseConstants())
            System.err.println("ERROR while initialising database constants");
    }

    private void start(String[] args) {
        try {
            if (args.length > 1 && args[0].equals("--update")) {
                loader.updateMichelinDataset(args[1]);
            } else if (args.length > 0 && args[0].equals("--update")) {
                loader.updateMichelinDataset(null);
            }
        } catch (IOException e) {
            System.err.println("Error occured during database update: " + e);
            System.err.println("Shutting down...");
            shutdown();
        }

        loader.initialise();

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
