package com.example.demo3;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class GUIController {

    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegister;
    @FXML
    private Button btnCerca;
    @FXML
    private Button btnEsplora;
    @FXML
    private Button btnEsci;

    @FXML
    private void onLoginClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
            Parent root = loader.load();

            // Prendo lo Stage corrente a partire da un nodo qualsiasi già in scena
            Stage stage = (Stage) btnRegister.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Registrazione");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCercaClick() {
        apriRestaurants(true, btnCerca);
    }

    @FXML
    private void onEsploraClick() {
        apriRestaurants(false, btnEsplora);
    }

    private void apriRestaurants(boolean modalitaRicerca, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Restaurants.fxml"));
            Parent root = loader.load();

            RestaurantsController controller = loader.getController();
            controller.inizializza(modalitaRicerca);

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            Scene scene = new Scene(root, 650, 500);
            stage.setScene(scene);
            stage.setTitle(modalitaRicerca ? "Cerca Ristorante" : "Esplora Ristoranti");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEsciClick() {
        System.out.println("Esci premuto - chiudo l'applicazione");
        Stage stage = (Stage) btnEsci.getScene().getWindow();
        stage.close();
    }
}
