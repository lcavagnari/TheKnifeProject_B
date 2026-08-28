package com.example.demo3;

import com.example.demo3.data.Session;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Button btnTrova;
    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
        User utente = Session.getCurrentUser();
        boolean isOwner = utente != null && utente.getRole() == UserRole.OWNER;

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

            Navigation.pushBack(() -> Navigation.navigateTo(stage, "Home.fxml", "The Knife", 650, 500));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Restaurants.fxml"));
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
    private void onLogoutClick() {
        Session.logout();
        Navigation.clearBackstack();

        Stage stage = (Stage) btnLogout.getScene().getWindow();
        Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 650, 500);
    }
}
