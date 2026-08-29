package it.uninsubria.laboratoriob.client.gui;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.gui.data.Session;
import it.uninsubria.laboratoriob.client.gui.data.SessionRepository;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;

public class LoginController {

    private final SessionRepository sessionRepository = new SessionRepository();
    private Runnable onCancelCallback;
    private Runnable onLoginSuccessCallback;

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

    public void setOnLoginSuccessCallback(Runnable callback) {
        this.onLoginSuccessCallback = callback;
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

        AuthServiceInter authService = RmiRepository.getAuthService();
        if (authService == null) authService = RmiRepository.lookupAuthService();
        if (authService == null) {
            errorLabel.setText("Servizio di autenticazione non disponibile.");
            return;
        }

        User utente;
        try {
            utente = authService.login(username, password);
        } catch (RemoteException e) {
            errorLabel.setText("Server non raggiungibile: " + e.getMessage());
            return;
        }

        if (utente == null || utente.isSystem()) {
            errorLabel.setText("Username o password errati.");
            return;
        }

        ClientDataStore dataStore = GuiContext.getDataStore();
        dataStore.switchUser(utente.getId());
        if (utente instanceof Owner owner) {
            dataStore.getOwnerDAO().cacheOnly(owner);
        } else if (utente instanceof Customer customer) {
            dataStore.getCustomerDAO().cacheOnly(customer);
        }

        Session.login(utente);
        sessionRepository.save(utente);
        errorLabel.getStyleClass().removeAll("label-error", "label-success");
        errorLabel.getStyleClass().add("label-success");
        errorLabel.setText("Login riuscito! Benvenuto " + utente.getName() + ".");
        System.out.println("Login riuscito per: " + username + " (" + utente.getRole() + ")");

        if (onLoginSuccessCallback != null) {
            onLoginSuccessCallback.run();
        }
    }

    @FXML
    private void onAnnullaClick() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }
}
