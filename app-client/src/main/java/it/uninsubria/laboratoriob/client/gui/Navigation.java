package it.uninsubria.laboratoriob.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gestisce la navigazione tra le schermate della GUI JavaFX, mantenendo
 * uno stack di operazioni di ritorno (backstack) per il pulsante "Indietro".
 */
public final class Navigation {

    private static final Deque<Runnable> backstack = new ArrayDeque<>();

    private Navigation() {
    }

    /**
     * Inserisce un'operazione di ritorno nello stack della navigazione.
     *
     * @param goBack runnable da eseguire quando l'utente preme "Indietro"
     */
    public static void pushBack(Runnable goBack) {
        backstack.push(goBack);
    }

    /**
     * Esegue l'ultima operazione di ritorno registrata nello stack, se disponibile.
     */
    public static void goBack() {
        if (!backstack.isEmpty())
            backstack.pop().run();
    }

    /**
     * Svuota completamente lo stack della navigazione.
     */
    public static void clearBackstack() {
        backstack.clear();
    }

    /**
     * Naviga a una nuova schermata caricando il file FXML specificato.
     *
     * @param stage   la finestra principale in cui caricare la scena
     * @param fxmlPath percorso relativo del file FXML (rispetto alla classe Navigation)
     * @param title   titolo della finestra
     * @param width   larghezza della finestra in pixel
     * @param height  altezza della finestra in pixel
     */
    public static void navigateTo(Stage stage, String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}