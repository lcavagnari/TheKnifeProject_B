package com.example.demo3;

import com.example.demo3.data.PasswordUtil;
import com.example.demo3.data.Session;
import com.example.demo3.data.UserRepository;
import it.uninsubria.laboratoriob.api.objects.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    private final UserRepository userRepository = new UserRepository();

    @FXML
    private Label errorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnAnnulla;

    @FXML
    private void onLoginClick() {
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText("");

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Inserisci username e password.");
            return;
        }

        Optional<User> utenteOpt = userRepository.findByUsername(username);
        if (utenteOpt.isEmpty()) {
            errorLabel.setText("Username non trovato.");
            return;
        }

        User utente = utenteOpt.get();
        boolean passwordOk = PasswordUtil.matches(password, utente.getPasswordSalt(), utente.getPasswordHash());
        if (!passwordOk) {
            errorLabel.setText("Password errata.");
            return;
        }

        // --- Login riuscito ---
        Session.login(utente);
        System.out.println("Login riuscito per: " + username + " (" + utente.getRole() + ")");

        errorLabel.setStyle("-fx-text-fill: #008000;");
        errorLabel.setText("Login riuscito! Benvenuto " + utente.getName() + ".");
    }

    @FXML
    private void onAnnullaClick() {
        tornaAlMenuPrincipale();
    }

    private void tornaAlMenuPrincipale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnAnnulla.getScene().getWindow();
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setTitle("The Knife Menu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
