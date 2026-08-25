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
    @FXML private VBox confirmPasswordWrapper;

    @FXML private TextField usernameField;
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField indirizzoField;
    @FXML private TextField cittaField;
    @FXML private TextField nazioneField;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label usernameError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label nomeError;
    @FXML private Label cognomeError;
    @FXML private Label dataNascitaError;
    @FXML private Label nazioneError;

    @FXML private Button btnRegistrati;
    @FXML private Button btnAnnulla;

    @FXML
    public void initialize() {
        roleClienteCard.setOnMouseClicked(e -> selectRole(true));
        roleGestoreCard.setOnMouseClicked(e -> selectRole(false));

        confirmPasswordWrapper.setTranslateY(-20);
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !confirmPasswordWrapper.isVisible()) {
                confirmPasswordWrapper.setVisible(true);
                confirmPasswordWrapper.setManaged(true);
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(e -> {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(200), confirmPasswordWrapper);
                    tt.setFromY(-20);
                    tt.setToY(0);
                    tt.play();
                });
                pause.play();
            } else if (newVal.isEmpty() && confirmPasswordWrapper.isVisible()) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), confirmPasswordWrapper);
                tt.setFromY(0);
                tt.setToY(-20);
                tt.setOnFinished(e -> {
                    confirmPasswordWrapper.setVisible(false);
                    confirmPasswordWrapper.setManaged(false);
                });
                tt.play();
            }
        });

        // Blur validation
        usernameField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validateUsername();
        });
        nomeField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validateNome();
        });
        cognomeField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validateCognome();
        });
        passwordField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validatePassword();
        });
        confirmPasswordField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validateConfirmPassword();
        });
        nazioneField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validateNazione();
        });

        // Clear errors on typing
        usernameField.textProperty().addListener((obs, old, n) -> { if (usernameError.isVisible()) clearFieldError(usernameError, usernameField); });
        nomeField.textProperty().addListener((obs, old, n) -> { if (nomeError.isVisible()) clearFieldError(nomeError, nomeField); });
        cognomeField.textProperty().addListener((obs, old, n) -> { if (cognomeError.isVisible()) clearFieldError(cognomeError, cognomeField); });
        passwordField.textProperty().addListener((obs, old, n) -> { if (passwordError.isVisible()) clearFieldError(passwordError, passwordField); });
        confirmPasswordField.textProperty().addListener((obs, old, n) -> { if (confirmPasswordError.isVisible()) clearFieldError(confirmPasswordError, confirmPasswordField); });
        nazioneField.textProperty().addListener((obs, old, n) -> { if (nazioneError.isVisible()) clearFieldError(nazioneError, nazioneField); });
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

    // --- Blur validation ---

    private void validateUsername() {
        String val = usernameField.getText() != null ? usernameField.getText().trim() : "";
        if (val.isEmpty()) return;
        if (val.length() < 4 || val.length() > 16) {
            showFieldError(usernameError, "Deve avere tra 4 e 16 caratteri.", usernameField);
        } else if (userRepository.existsByUsername(val)) {
            showFieldError(usernameError, "Username non disponibile.", usernameField);
        } else {
            clearFieldError(usernameError, usernameField);
        }
    }

    private void validateNome() {
        String val = nomeField.getText() != null ? nomeField.getText().trim() : "";
        if (val.isEmpty()) {
            showFieldError(nomeError, "Il nome è obbligatorio.", nomeField);
        } else {
            clearFieldError(nomeError, nomeField);
        }
    }

    private void validateCognome() {
        String val = cognomeField.getText() != null ? cognomeField.getText().trim() : "";
        if (val.isEmpty()) {
            showFieldError(cognomeError, "Il cognome è obbligatorio.", cognomeField);
        } else {
            clearFieldError(cognomeError, cognomeField);
        }
    }

    private void validatePassword() {
        String val = passwordField.getText() != null ? passwordField.getText() : "";
        if (val.isEmpty()) return;
        if (val.length() < 4) {
            showFieldError(passwordError, "Minimo 4 caratteri.", passwordField);
        } else {
            clearFieldError(passwordError, passwordField);
        }
    }

    private void validateConfirmPassword() {
        String pw = passwordField.getText() != null ? passwordField.getText() : "";
        String confirm = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";
        if (confirm.isEmpty()) return;
        if (!pw.equals(confirm)) {
            showFieldError(confirmPasswordError, "Le password non corrispondono.", confirmPasswordField);
        } else {
            clearFieldError(confirmPasswordError, confirmPasswordField);
        }
    }

    private void validateNazione() {
        String val = nazioneField.getText() != null ? nazioneField.getText().trim() : "";
        if (val.isEmpty()) {
            clearFieldError(nazioneError, nazioneField);
            return;
        }
        if (Nation.fromString(val) == null) {
            showFieldError(nazioneError, "Nazione non riconosciuta.", nazioneField);
        } else {
            clearFieldError(nazioneError, nazioneField);
        }
    }

    // --- Submit ---

    @FXML
    private void onRegistratiClick() {
        clearAllErrors();
        errorLabel.setText("");

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

        boolean valid = true;

        if (username.length() < 4 || username.length() > 16) {
            showFieldError(usernameError, "Deve avere tra 4 e 16 caratteri.", usernameField);
            valid = false;
        } else if (userRepository.existsByUsername(username)) {
            showFieldError(usernameError, "Username non disponibile.", usernameField);
            valid = false;
        }

        if (nome.isEmpty()) {
            showFieldError(nomeError, "Il nome è obbligatorio.", nomeField);
            valid = false;
        }

        if (cognome.isEmpty()) {
            showFieldError(cognomeError, "Il cognome è obbligatorio.", cognomeField);
            valid = false;
        }

        if (dataNascita == null) {
            showFieldError(dataNascitaError, "Seleziona la data di nascita.", dataNascitaPicker);
            valid = false;
        }

        if (password.length() < 4) {
            showFieldError(passwordError, "Minimo 4 caratteri.", passwordField);
            valid = false;
        }

        if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordError, "Le password non corrispondono.", confirmPasswordField);
            valid = false;
        }

        Nation nazione = Nation.fromString(nazioneTesto);
        if (!nazioneTesto.isEmpty() && nazione == null) {
            showFieldError(nazioneError, "Nazione non riconosciuta.", nazioneField);
            valid = false;
        }

        if (!valid) return;

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
            errorLabel.getStyleClass().removeAll("label-error", "label-success");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Errore durante il salvataggio dei dati dell'utente.");
        }
    }

    @FXML
    private void onAnnullaClick() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }

    // --- Error helpers ---

    private void showFieldError(Label errorLabel, String message, Control field) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
    }

    private void clearFieldError(Label errorLabel, Control field) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        field.getStyleClass().remove("error");
    }

    private void clearAllErrors() {
        Label[] errors = {usernameError, passwordError, confirmPasswordError, nomeError, cognomeError, dataNascitaError, nazioneError};
        Control[] fields = {usernameField, passwordField, confirmPasswordField, nomeField, cognomeField, dataNascitaPicker, nazioneField};
        for (int i = 0; i < errors.length; i++) {
            errors[i].setVisible(false);
            errors[i].setManaged(false);
            fields[i].getStyleClass().remove("error");
        }
    }
}
