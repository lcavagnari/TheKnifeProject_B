package it.uninsubria.laboratoriob.client.gui.controller;

import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.client.gui.Navigation;
import it.uninsubria.laboratoriob.client.gui.session.Session;
import it.uninsubria.laboratoriob.client.gui.session.SessionRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contenuto "loggato" iniettato nella menu-card di GUI.fxml al posto di
 * titleBlock/menuButtons, esattamente come Login.fxml e Register.fxml.
 */
public class HomeController {

    private final SessionRepository sessionRepository = new SessionRepository();
    private Runnable onDisconnettiCallback;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Button btnTrova;
    @FXML
    private Button btnMieiRistoranti;
    @FXML
    private Button btnPreferiti;
    @FXML
    private Button btnDisconnetti;
    @FXML
    private Button btnEsci;

    public void setOnDisconnettiCallback(Runnable callback) {
        this.onDisconnettiCallback = callback;
    }

    /**
     * Da richiamare ogni volta prima di mostrare questo componente: rilegge
     * l'utente corrente dalla Session, dato che il nodo viene precaricato una
     * sola volta e riutilizzato per tutta la sessione dell'app.
     */
    public void aggiorna() {
        User utente = Session.getCurrentUser();
        boolean isOwner = utente != null && utente.getRole() == UserRole.OWNER;
        boolean isCliente = utente != null && utente.getRole() == UserRole.CLIENT;
        btnMieiRistoranti.setVisible(isOwner);
        btnMieiRistoranti.setManaged(isOwner);
        btnPreferiti.setVisible(isCliente);
        btnPreferiti.setManaged(isCliente);

        if (utente == null) {
            welcomeLabel.setText("Benvenuto");
            roleLabel.setText("");
            return;
        }

        welcomeLabel.setText("Benvenuto, " + utente.getName() + "!");
        roleLabel.setText(isOwner ? "Proprietario" : "Cliente");
    }

    @FXML
    private void onTrovaClick() {
        try {
            Stage stage = (Stage) btnTrova.getScene().getWindow();

            Navigation.pushBack(() -> Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 650, 500));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/Restaurants.fxml"));
            Parent root = loader.load();

            RestaurantsController controller = loader.getController();
            controller.inizializza();

            Scene scene = new Scene(root, 650, 500);
            stage.setScene(scene);
            stage.setTitle("Trova Ristoranti");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMieiRistorantiClick() {
        if (!(Session.getCurrentUser() instanceof Owner owner))
            return;
        try {
            Stage stage = (Stage) btnMieiRistoranti.getScene().getWindow();

            Navigation.pushBack(() -> Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 650, 500));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/Restaurants.fxml"));
            Parent root = loader.load();

            RestaurantsController controller = loader.getController();
            controller.inizializzaProprietario(owner);

            Scene scene = new Scene(root, 650, 500);
            stage.setScene(scene);
            stage.setTitle("I Miei Ristoranti");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onPreferitiClick() {
        try {
            Stage stage = (Stage) btnPreferiti.getScene().getWindow();

            Navigation.pushBack(() -> Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 650, 500));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/Favorites.fxml"));
            Parent root = loader.load();

            FavoritesController controller = loader.getController();
            controller.inizializza();

            Scene scene = new Scene(root, 650, 500);
            stage.setScene(scene);
            stage.setTitle("Preferiti");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDisconnettiClick() {
        Session.logout();
        sessionRepository.clear();
        if (onDisconnettiCallback != null)
            onDisconnettiCallback.run();
    }

    @FXML
    private void onEsciClick() {
        Stage stage = (Stage) btnEsci.getScene().getWindow();
        stage.close();
    }
}
