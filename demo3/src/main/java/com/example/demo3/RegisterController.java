package com.example.demo3;

import com.example.demo3.data.PasswordUtil;
import com.example.demo3.data.UserRepository;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.time.LocalDate;

public class RegisterController {

    private final UserRepository userRepository = new UserRepository();
    private Runnable onCancelCallback;

    @FXML private Label errorLabel;
    @FXML private RadioButton radioGestore;
    @FXML private RadioButton radioCliente;
    @FXML private ToggleGroup tipoUtenteGroup;
    @FXML private VBox roleClienteCard;
    @FXML private VBox roleGestoreCard;

    @FXML private TextField usernameField;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField indirizzoField;
    @FXML private TextField cittaField;
    @FXML private TextField nazioneField;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button btnRegistrati;
    @FXML private Button btnAnnulla;

    @FXML
    public void initialize() {
        roleClienteCard.setOnMouseClicked(e -> selectRole(true));
        roleGestoreCard.setOnMouseClicked(e -> selectRole(false));

        confirmPasswordField.setTranslateY(-20);
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !confirmPasswordField.isVisible()) {
                confirmPasswordField.setVisible(true);
                confirmPasswordField.setManaged(true);
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(e -> {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(200), confirmPasswordField);
                    tt.setFromY(-20);
                    tt.setToY(0);
                    tt.play();
                });
                pause.play();
            } else if (newVal.isEmpty() && confirmPasswordField.isVisible()) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), confirmPasswordField);
                tt.setFromY(0);
                tt.setToY(-20);
                tt.setOnFinished(e -> {
                    confirmPasswordField.setVisible(false);
                    confirmPasswordField.setManaged(false);
                });
                tt.play();
            }
        });
    }

    private void selectRole(boolean cliente) {
        if (cliente) {
            radioCliente.setSelected(true);
            roleClienteCard.getStyleClass().setAll("role-card", "role-card-selected");
            roleGestoreCard.getStyleClass().setAll("role-card");
        } else {
            radioGestore.setSelected(true);
            roleGestoreCard.getStyleClass().setAll("role-card", "role-card-selected");
            roleClienteCard.getStyleClass().setAll("role-card");
        }
    }

    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }

    @FXML
    private void onRegistratiClick() {
        errorLabel.getStyleClass().removeAll("label-success", "label-error");
        errorLabel.getStyleClass().add("label-error");
        errorLabel.setText("");
        clearErrors();

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String nome = nomeField.getText() != null ? nomeField.getText().trim() : "";
        String cognome = cognomeField.getText() != null ? cognomeField.getText().trim() : "";
        String indirizzo = indirizzoField.getText() != null ? indirizzoField.getText().trim() : "";
        String citta = cittaField.getText() != null ? cittaField.getText().trim() : "";
        String nazioneTesto = nazioneField.getText() != null ? nazioneField.getText().trim() : "";
        LocalDate dataNascita = dataNascitaPicker.getValue();
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

        boolean isGestore = radioGestore != null && radioGestore.isSelected();

        if (username.length() < 4 || username.length() > 16) {
            showError("Username non valido: deve avere tra 4 e 16 caratteri.", usernameField);
            return;
        }
        if (userRepository.existsByUsername(username)) {
            showError("Username non disponibile, scegline un altro.", usernameField);
            return;
        }
        if (nome.isEmpty()) {
            showError("Il nome e obbligatorio.", nomeField);
            return;
        }
        if (cognome.isEmpty()) {
            showError("Il cognome e obbligatorio.", cognomeField);
            return;
        }
        if (dataNascita == null) {
            showError("Inserisci la data di nascita.", dataNascitaPicker);
            return;
        }
        if (password.length() < 4) {
            showError("La password deve avere almeno 4 caratteri.", passwordField);
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Le password non corrispondono.", confirmPasswordField);
            return;
        }

        Nation nazione = Nation.fromString(nazioneTesto);
        if (!nazioneTesto.isEmpty() && nazione == null) {
            showError("Nazione non riconosciuta: " + nazioneTesto, nazioneField);
            return;
        }

        Location location = new Location(nazione, citta, 0.0, 0.0, indirizzo);

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hash(password, salt);

        User nuovoUtente = isGestore
                ? new Owner(username, passwordHash, salt, nome, cognome, location, dataNascita)
                : new Customer(username, passwordHash, salt, nome, cognome, location, dataNascita);

        boolean salvato = userRepository.save(nuovoUtente);

        if (salvato) {
            errorLabel.getStyleClass().removeAll("label-error", "label-success");
            errorLabel.getStyleClass().add("label-success");
            errorLabel.setText("Registrazione completata! File salvato in data/users.");
        } else {
            errorLabel.setText("Errore durante il salvataggio dei dati dell'utente.");
        }
    }

    @FXML
    private void onAnnullaClick() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }

    private void showError(String message, Control field) {
        errorLabel.setText(message);
        if (field != null) {
            field.getStyleClass().add("error");
        }
    }

    private void clearErrors() {
        Control[] fields = {
            usernameField, nomeField, cognomeField, passwordField,
            confirmPasswordField, dataNascitaPicker, nazioneField
        };
        for (Control field : fields) {
            if (field != null) {
                field.getStyleClass().remove("error");
            }
        }
    }
}
