package it.uninsubria.laboratoriob.client.gui;

import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.utils.HeartbeatClient;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static final String SERVER_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final int HEARTBEAT_PORT = 5555;
    private static final long HEARTBEAT_INTERVAL_MINUTES = 5;

    @Override
    public void start(Stage stage) throws IOException {
        RmiRepository.configure(SERVER_HOST, RMI_PORT);
        GuiContext.init(new ClientDataStore());

        HeartbeatClient heartbeatClient = new HeartbeatClient(SERVER_HOST, HEARTBEAT_PORT, HEARTBEAT_INTERVAL_MINUTES);
        heartbeatClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(heartbeatClient::shutdown));

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("The Knife Menu");
        stage.setScene(scene);
        stage.setMaximized(true);  // <-- This line opens the window maximized
        stage.show();
    }
}
