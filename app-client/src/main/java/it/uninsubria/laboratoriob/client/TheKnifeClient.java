package it.uninsubria.laboratoriob.client;

import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.ui.menus.GuestMenus;
import it.uninsubria.laboratoriob.client.utils.HeartbeatClient;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;

/**
 * Classe principale del client The Knife.
 * <p>
 * Inizializza il {@link ClientDataStore} e fornisce un punto di accesso
 * ai dati memorizzati localmente in cache JSON.
 * </p>
 */
public class TheKnifeClient {

    // TODO: ensure all files have copyright notice and javadoc comments
    // TODO: update technical documentation.
    // TOOD: resolve lint.

    public static void main(String[] args) {
        IO.printSuccessMessage("Loading The Knife Client...");

        TheKnifeClient client = new TheKnifeClient();

        IO.printSuccessMessage("Client initialized. Data store ready.");

        new GuestMenus(client.dataStore).openMenu();
    }

    private static final String serverHost = "localhost";
    private static final int rmiPort = 1099;

    private static final int heartbeatPort = 5555;
    private static final long heartbeatIntervalMinutes = 5;

    private final HeartbeatClient tcpHbeatClient;
    private final ClientDataStore dataStore;

    public TheKnifeClient() {
        RmiRepository.configure(serverHost, rmiPort);

        this.tcpHbeatClient = new HeartbeatClient(serverHost, heartbeatPort, heartbeatIntervalMinutes);
        this.dataStore = new ClientDataStore();

        tcpHbeatClient.start();
        dataStore.acquireRemoteServices();

        Runtime.getRuntime().addShutdownHook(new Thread(tcpHbeatClient::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(IO::closeScanner));
    }
}
