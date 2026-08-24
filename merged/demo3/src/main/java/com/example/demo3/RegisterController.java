package com.example.demo3;

import com.example.demo3.data.PasswordUtil;
import com.example.demo3.data.UserRepository;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class RegisterController {

    private final UserRepository userRepository = new UserRepository();

    @FXML private Label errorLabel;
    @FXML private RadioButton radioGestore;
    @FXML private RadioButton radioCliente;
    @FXML private ToggleGroup tipoUtenteGroup;

    @FXML private TextField usernameField;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField indirizzoField;
    @FXML private TextField cittaField;
    @FXML private TextField nazioneField;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private PasswordField passwordField;

    @FXML private Button btnRegistrati;
    @FXML private Button btnAnnulla;

    @FXML
    private void onRegistratiClick() {
        // Reset label e stili dei campi
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText("");
        resetStyles(usernameField, nomeField, cognomeField, passwordField, dataNascitaPicker);

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String nome = nomeField.getText() != null ? nomeField.getText().trim() : "";
        String cognome = cognomeField.getText() != null ? cognomeField.getText().trim() : "";
        String indirizzo = indirizzoField.getText() != null ? indirizzoField.getText().trim() : "";
        String citta = cittaField.getText() != null ? cittaField.getText().trim() : "";
        String nazioneTesto = nazioneField.getText() != null ? nazioneField.getText().trim() : "";
        LocalDate dataNascita = dataNascitaPicker.getValue();
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        boolean isGestore = radioGestore != null && radioGestore.isSelected();

        // --- VALIDAZIONI UX ---
        if (username.length() < 4 || username.length() > 16) {
            errorLabel.setText("Username non valido: deve avere tra 4 e 16 caratteri.");
            setErrorStyle(usernameField);
            return;
        }
        if (userRepository.existsByUsername(username)) {
            errorLabel.setText("Username non disponibile, scegline un altro.");
            setErrorStyle(usernameField);
            return;
        }
        if (nome.isEmpty()) {
            errorLabel.setText("Il nome è obbligatorio.");
            setErrorStyle(nomeField);
            return;
        }
        if (cognome.isEmpty()) {
            errorLabel.setText("Il cognome è obbligatorio.");
            setErrorStyle(cognomeField);
            return;
        }
        if (dataNascita == null) {
            errorLabel.setText("Inserisci la data di nascita.");
            setErrorStyle(dataNascitaPicker);
            return;
        }
        if (password.length() < 4) {
            errorLabel.setText("La password deve avere almeno 4 caratteri.");
            setErrorStyle(passwordField);
            return;
        }

        Nation nazione = Nation.fromString(nazioneTesto);
        if (!nazioneTesto.isEmpty() && nazione == null) {
            errorLabel.setText("Nazione non riconosciuta: " + nazioneTesto);
            setErrorStyle(nazioneField);
            return;
        }

        // --- COSTRUZIONE ENTITÀ (common-api) E SALVATAGGIO ---
        Location location = new Location(nazione, citta, 0.0, 0.0, indirizzo);

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hash(password, salt);

        User nuovoUtente = isGestore
                ? new Owner(username, passwordHash, salt, nome, cognome, location, dataNascita)
                : new Customer(username, passwordHash, salt, nome, cognome, location, dataNascita);

        boolean salvato = userRepository.save(nuovoUtente);

        if (salvato) {
            errorLabel.setStyle("-fx-text-fill: #2ecc71;");
            errorLabel.setText("Registrazione completata! File salvato in data/users.");
        } else {
            errorLabel.setText("Errore durante il salvataggio dei dati dell'utente.");
        }
    }

    @FXML
    private void onAnnullaClick() {
        tornaAlMenuPrincipale(btnAnnulla);
    }

    private void tornaAlMenuPrincipale(Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setTitle("The Knife Menu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossibile caricare il menu principale.");
        }
    }

    private void setErrorStyle(Control field) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px; -fx-border-radius: 3px;");
    }

    private void resetStyles(Control... fields) {
        for (Control field : fields) {
            field.setStyle("");
        }
    }
}
