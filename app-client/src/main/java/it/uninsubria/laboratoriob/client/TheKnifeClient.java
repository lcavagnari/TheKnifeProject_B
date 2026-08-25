package it.uninsubria.laboratoriob.client;

import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.ui.menus.GuestMenus;
import it.uninsubria.laboratoriob.client.utils.HeartbeatClient;

/**
 * Classe principale del client The Knife.
 * <p>
 * Inizializza il {@link ClientDataStore} e fornisce un punto di accesso
 * ai dati memorizzati localmente in cache JSON.
 * </p>
 * <p>
 * In futuro, qui verrà integrato il layer di comunicazione RMI
 * per sincronizzare i dati con il server.
 * </p>
 */
public class TheKnifeClient {

    public static void main(String[] args) {
        IO.printSuccessMessage("Loading The Knife Client...");

        TheKnifeClient client = new TheKnifeClient();

        IO.printSuccessMessage("Client initialized. Data store ready.");

        new GuestMenus(client.dataStore).openMenu();
    }

    private static final String serverHost = "localhost";
    private static final int heartbeatPort = 5555;
    private static final long heartbeatIntervalMinutes = 5;

    private final HeartbeatClient tcpHbeatClient;
    private final ClientDataStore dataStore;

    public TheKnifeClient() {
        this.dataStore = new ClientDataStore();
        this.tcpHbeatClient = new HeartbeatClient(serverHost, heartbeatPort, heartbeatIntervalMinutes);

        tcpHbeatClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(tcpHbeatClient::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(IO::closeScanner));
    }

    public void shutdown() {
        tcpHbeatClient.shutdown();
        IO.closeScanner();
    }
}
