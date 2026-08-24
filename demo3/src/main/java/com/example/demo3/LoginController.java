package com.example.demo3;

import com.example.demo3.data.PasswordUtil;
import com.example.demo3.data.Session;
import com.example.demo3.data.UserRepository;
import it.uninsubria.laboratoriob.api.objects.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    private final UserRepository userRepository = new UserRepository();
    private Runnable onCancelCallback;

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

    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }

    @FXML
    private void onLoginClick() {
        errorLabel.getStyleClass().removeAll("label-success", "label-error");
        errorLabel.getStyleClass().add("label-error");
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

        Session.login(utente);
        errorLabel.getStyleClass().removeAll("label-error", "label-success");
        errorLabel.getStyleClass().add("label-success");
        errorLabel.setText("Login riuscito! Benvenuto " + utente.getName() + ".");
        System.out.println("Login riuscito per: " + username + " (" + utente.getRole() + ")");
    }

    @FXML
    private void onAnnullaClick() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }
}
