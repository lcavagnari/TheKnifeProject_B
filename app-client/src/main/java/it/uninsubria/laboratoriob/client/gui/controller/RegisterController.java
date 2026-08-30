package it.uninsubria.laboratoriob.client.gui.controller;

import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.gui.GuiContext;
import it.uninsubria.laboratoriob.client.gui.session.Session;
import it.uninsubria.laboratoriob.client.gui.session.SessionRepository;
import it.uninsubria.laboratoriob.client.utils.RmiRepository;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.rmi.RemoteException;
import java.time.LocalDate;

public class RegisterController {

    private final SessionRepository sessionRepository = new SessionRepository();
    private Runnable onCancelCallback;
    private Runnable onRegisterSuccessCallback;

    @FXML
    private Label errorLabel;
    @FXML
    private RadioButton radioGestore;
    @FXML
    private RadioButton radioCliente;
    @FXML
    private ToggleGroup tipoUtenteGroup;
    @FXML
    private VBox roleClienteCard;
    @FXML
    private VBox roleGestoreCard;
    @FXML
    private VBox confirmPasswordWrapper;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField nomeField;
    @FXML
    private TextField cognomeField;
    @FXML
    private TextField indirizzoField;
    @FXML
    private TextField cittaField;
    @FXML
    private TextField nazioneField;
    @FXML
    private DatePicker dataNascitaPicker;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label usernameError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmPasswordError;
    @FXML
    private Label nomeError;
    @FXML
    private Label cognomeError;
    @FXML
    private Label dataNascitaError;
    @FXML
    private Label nazioneError;

    @FXML
    private Button btnRegistrati;
    @FXML
    private Button btnAnnulla;

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
        dataNascitaPicker.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validateDataNascita();
        });

        // Clear errors on typing
        usernameField.textProperty().addListener((obs, old, n) -> {
            if (usernameError.isVisible()) clearFieldError(usernameError, usernameField);
        });
        nomeField.textProperty().addListener((obs, old, n) -> {
            if (nomeError.isVisible()) clearFieldError(nomeError, nomeField);
        });
        cognomeField.textProperty().addListener((obs, old, n) -> {
            if (cognomeError.isVisible()) clearFieldError(cognomeError, cognomeField);
        });
        passwordField.textProperty().addListener((obs, old, n) -> {
            if (passwordError.isVisible()) clearFieldError(passwordError, passwordField);
        });
        confirmPasswordField.textProperty().addListener((obs, old, n) -> {
            if (confirmPasswordError.isVisible()) clearFieldError(confirmPasswordError, confirmPasswordField);
        });
        nazioneField.textProperty().addListener((obs, old, n) -> {
            if (nazioneError.isVisible()) clearFieldError(nazioneError, nazioneField);
        });
        dataNascitaPicker.valueProperty().addListener((obs, old, n) -> {
            if (dataNascitaError.isVisible()) clearFieldError(dataNascitaError, dataNascitaPicker);
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

    public void setOnRegisterSuccessCallback(Runnable callback) {
        this.onRegisterSuccessCallback = callback;
    }

    // --- Blur validation ---

    private void validateUsername() {
        String val = usernameField.getText() != null ? usernameField.getText().trim() : "";
        if (val.isEmpty()) return;
        if (!val.matches("[a-zA-Z0-9_]+"))
            showFieldError(usernameError, "Solo lettere, numeri e underscore.", usernameField);
        else if (val.length() < 4 || val.length() > 16)
            showFieldError(usernameError, "Deve avere tra 4 e 16 caratteri.", usernameField);
        else
            clearFieldError(usernameError, usernameField);
    }

    private void validateNome() {
        String val = nomeField.getText() != null ? nomeField.getText().trim() : "";
        if (val.isEmpty())
            showFieldError(nomeError, "Il nome è obbligatorio.", nomeField);
        else if (!val.matches("[a-zA-ZÀ-ÿ'\\s-]+"))
            showFieldError(nomeError, "Solo lettere, spazi, trattini e apostrofi.", nomeField);
        else if (val.length() < 4)
            showFieldError(nomeError, "Deve avere almeno 4 caratteri.", nomeField);
        else
            clearFieldError(nomeError, nomeField);
    }

    private void validateCognome() {
        String val = cognomeField.getText() != null ? cognomeField.getText().trim() : "";
        if (val.isEmpty())
            showFieldError(cognomeError, "Il cognome è obbligatorio.", cognomeField);
        else if (!val.matches("[a-zA-ZÀ-ÿ'\\s-]+"))
            showFieldError(cognomeError, "Solo lettere, spazi, trattini e apostrofi.", cognomeField);
        else if (val.length() < 4)
            showFieldError(cognomeError, "Deve avere almeno 4 caratteri.", cognomeField);
        else
            clearFieldError(cognomeError, cognomeField);
    }

    private void validatePassword() {
        String val = passwordField.getText() != null ? passwordField.getText() : "";
        if (val.isEmpty()) return;
        if (val.length() < 4)
            showFieldError(passwordError, "Minimo 4 caratteri.", passwordField);
        else
            clearFieldError(passwordError, passwordField);
    }

    private void validateConfirmPassword() {
        String pw = passwordField.getText() != null ? passwordField.getText() : "";
        String confirm = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";
        if (confirm.isEmpty()) return;
        if (!pw.equals(confirm))
            showFieldError(confirmPasswordError, "Le password non corrispondono.", confirmPasswordField);
        else
            clearFieldError(confirmPasswordError, confirmPasswordField);
    }

    private void validateDataNascita() {
        LocalDate val = dataNascitaPicker.getValue();
        if (val == null) return;
        if (val.isAfter(LocalDate.now()))
            showFieldError(dataNascitaError, "La data non può essere nel futuro.", dataNascitaPicker);
        else if (val.isAfter(LocalDate.now().minusYears(13)))
            showFieldError(dataNascitaError, "Devi avere almeno 13 anni.", dataNascitaPicker);
        else
            clearFieldError(dataNascitaError, dataNascitaPicker);
    }

    private void validateNazione() {
        String val = nazioneField.getText() != null ? nazioneField.getText().trim() : "";
        if (val.isEmpty()) {
            clearFieldError(nazioneError, nazioneField);
            return;
        }
        if (Nation.fromString(val) == null)
            showFieldError(nazioneError, "Nazione non riconosciuta.", nazioneField);
        else
            clearFieldError(nazioneError, nazioneField);
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

        // Validate that a role is selected
        if (!radioCliente.isSelected() && !radioGestore.isSelected()) {
            errorLabel.getStyleClass().removeAll("label-success", "label-error");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Seleziona un tipo di utente (Cliente o Gestore).");
            valid = false;
        }

        if (!username.matches("[a-zA-Z0-9_]+")) {
            showFieldError(usernameError, "Solo lettere, numeri e underscore.", usernameField);
            valid = false;
        } else if (username.length() < 4 || username.length() > 16) {
            showFieldError(usernameError, "Deve avere tra 4 e 16 caratteri.", usernameField);
            valid = false;
        }

        if (nome.isEmpty()) {
            showFieldError(nomeError, "Il nome è obbligatorio.", nomeField);
            valid = false;
        } else if (!nome.matches("[a-zA-ZÀ-ÿ'\\s-]+")) {
            showFieldError(nomeError, "Solo lettere, spazi, trattini e apostrofi.", nomeField);
            valid = false;
        } else if (nome.length() < 4) {
            showFieldError(nomeError, "Deve avere almeno 4 caratteri.", nomeField);
            valid = false;
        }

        if (cognome.isEmpty()) {
            showFieldError(cognomeError, "Il cognome è obbligatorio.", cognomeField);
            valid = false;
        } else if (!cognome.matches("[a-zA-ZÀ-ÿ'\\s-]+")) {
            showFieldError(cognomeError, "Solo lettere, spazi, trattini e apostrofi.", cognomeField);
            valid = false;
        } else if (cognome.length() < 4) {
            showFieldError(cognomeError, "Deve avere almeno 4 caratteri.", cognomeField);
            valid = false;
        }

        if (dataNascita == null) {
            showFieldError(dataNascitaError, "Seleziona la data di nascita.", dataNascitaPicker);
            valid = false;
        } else if (dataNascita.isAfter(LocalDate.now())) {
            showFieldError(dataNascitaError, "La data non può essere nel futuro.", dataNascitaPicker);
            valid = false;
        } else if (dataNascita.isAfter(LocalDate.now().minusYears(13))) {
            showFieldError(dataNascitaError, "Devi avere almeno 13 anni.", dataNascitaPicker);
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

        if (!valid) {
            shakeAllErrors();
            return;
        }

        Location location = new Location(nazione, citta, 0.0, 0.0, indirizzo);

        AuthServiceInter authService = RmiRepository.getAuthService();
        if (authService == null) authService = RmiRepository.lookupAuthService();
        if (authService == null) {
            errorLabel.getStyleClass().removeAll("label-success");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Servizio di autenticazione non disponibile.");
            return;
        }

        User nuovoUtente;
        try {
            nuovoUtente = authService.register(username, password, nome, cognome, dataNascita, location, isGestore);
        } catch (RemoteException e) {
            errorLabel.getStyleClass().removeAll("label-success");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Registrazione fallita: " + e.getMessage());
            return;
        }

        if (nuovoUtente == null) {
            errorLabel.getStyleClass().removeAll("label-success");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Registrazione fallita: username non disponibile o dati non validi.");
            return;
        }

        ClientDataStore dataStore = GuiContext.getDataStore();
        dataStore.switchUser(nuovoUtente.getId());

        boolean cached = isGestore
                ? dataStore.getOwnerDAO().cacheOnly((Owner) nuovoUtente)
                : dataStore.getCustomerDAO().cacheOnly((Customer) nuovoUtente);

        if (!cached) {
            errorLabel.getStyleClass().removeAll("label-success");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Registrazione riuscita ma impossibile salvare la cache locale.");
            return;
        }

        Session.login(nuovoUtente);
        sessionRepository.save(nuovoUtente);
        errorLabel.getStyleClass().removeAll("label-error", "label-success");
        errorLabel.getStyleClass().add("label-success");
        errorLabel.setText("Registrazione completata! Benvenuto " + nuovoUtente.getName() + ".");

        if (onRegisterSuccessCallback != null)
            onRegisterSuccessCallback.run();
    }

    @FXML
    private void onAnnullaClick() {
        if (onCancelCallback != null)
            onCancelCallback.run();
    }

    // --- Error helpers ---

    private void showFieldError(Label errorLabel, String message, Control field) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("error"))
            field.getStyleClass().add("error");
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

    private void shakeField(Control field) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), field);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setByX(6);
        shake.setOnFinished(e -> field.setTranslateX(0));
        shake.play();
    }

    private void shakeAllErrors() {
        btnRegistrati.setDisable(true);
        Control[] fields = {usernameField, passwordField, confirmPasswordField, nomeField, cognomeField, dataNascitaPicker, nazioneField};
        int count = 0;
        for (Control f : fields) {
            if (f.getStyleClass().contains("error")) {
                shakeField(f);
                count++;
            }
        }
        if (count == 0) {
            btnRegistrati.setDisable(false);
            return;
        }
        PauseTransition pause = new PauseTransition(Duration.millis(350));
        pause.setOnFinished(e -> btnRegistrati.setDisable(false));
        pause.play();
    }
}
