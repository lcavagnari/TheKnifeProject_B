package it.uninsubria.laboratoriob.client;

import it.uninsubria.laboratoriob.client.cli.IO;
import it.uninsubria.laboratoriob.client.cli.menus.GuestMenus;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.gui.GuiContext;
import it.uninsubria.laboratoriob.client.utils.HeartbeatClient;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

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
public class TheKnifeClient extends Application {

    private static ClientDataStore clientDataStore;

    static final String SERVER_HOST = "localhost";
    static final int RMI_PORT = 1099;
    static final int HEARTBEAT_PORT = 5555;
    static final long HEARTBEAT_INTERVAL_MINUTES = 5;

    /** Punto di ingresso dell'applicazione client. */
    public static void main(String[] args) {
        IO.printSuccessMessage("Loading The Knife Client...");

        initClient();

        IO.printSuccessMessage("Client initialized. Data store ready.");

        if (Arrays.asList(args).contains("--cli")) new GuestMenus(clientDataStore).openMenu();
        else Application.launch(TheKnifeClient.class, args);
    }

    private static void initClient() {
        RmiRepository.configure(SERVER_HOST, RMI_PORT);
        clientDataStore = new ClientDataStore();

        HeartbeatClient heartbeat = new HeartbeatClient(SERVER_HOST, HEARTBEAT_PORT, HEARTBEAT_INTERVAL_MINUTES);
        heartbeat.start();

        Runtime.getRuntime().addShutdownHook(new Thread(heartbeat::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(IO::closeScanner));
    }

    @Override
    public void start(Stage stage) throws IOException {
        GuiContext.init(clientDataStore);

        FXMLLoader fxmlLoader = new FXMLLoader(TheKnifeClient.class.getResource("gui/GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("The Knife");
        stage.setScene(scene);

        stage.setMaximized(true);
        stage.toFront();
        stage.show();
    }
}
