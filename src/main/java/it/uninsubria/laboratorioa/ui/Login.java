package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.users.Client;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.Loader;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe di utility per la gestione del login e della registrazione utenti.
 * <p>
 * Contiene metodi statici per facilitare le operazioni di autenticazione e
 * gestione degli utenti nel sistema.
 * </p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
@UtilityClass
public class Login {

    public static User register() {
        IO.clearScreen();
        System.out.println("=== User Registration ===");

        boolean isOwner = IO.getBooleanInput("Benvenuto! Sei un gestore o un cliente? [Sì - Gestore, No - Cliente]:");

        String username = null;
        while (username == null) {
            try {
                username = IO.getUserInput("Scegli un nome utente [4-16 caratteri]:").trim();
                if (Loader.getUsersByName().containsKey(username))
                    throw new IllegalArgumentException("Username non disponibile");

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{3,39}$", username);
            } catch (IllegalArgumentException ex) {
                username = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
                return null;
            }
        }

        String firstName = null, lastName = null;
        while (firstName == null || lastName == null) {
            try {
                firstName = IO.getUserInput("Inserisci il tuo nome:").trim();
                lastName = IO.getUserInput("Inserisci il tuo cognome:").trim();

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", firstName);
                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", lastName);
            } catch (IllegalArgumentException ex) {
                firstName = null;
                lastName = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
                return null;
            }
        }

        Location location = null;
        try {
            location = IO.getLocationInput(true);
        } catch (AbortOperationException ignored) {

        }

        LocalDate dateOfBirth = null;
        while (dateOfBirth == null) {
            try {
                dateOfBirth = LocalDate.parse(IO.getUserInput("Inserisci la tua data di nascita (YYYY-MM-DD):"));
                IO.validateDates(LocalDateTime.MIN, LocalDateTime.now(), dateOfBirth.atStartOfDay());
            } catch (IllegalArgumentException ex) {
                dateOfBirth = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
                return null;
            }
        }

        String password = null;
        while (password == null) {
            try {
                password = IO.getUserInput("Inserisci una password (min 4 caratteri):").trim();
                IO.validateString(".{4,}", password);
            } catch (IllegalArgumentException ex) {
                password = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
                return null;
            }
        }

        try {
            User user = isOwner
                    ? new Owner(username, password, firstName, lastName, location, dateOfBirth)
                    : new Client(username, password, firstName, lastName, location, dateOfBirth);

            if (IO.validateUser(user))
                IO.getUserInput("Registrazione completata! Premi Invio per tornare al menu principale.");

            Loader.getUsersByName().put(username, user);
            Loader.getUsersById().put(user.getId(), user);
            user.save();

            return user;

        } catch (IllegalArgumentException ex) {
            IO.printErrorMessage(ex.getMessage() + ". Ritorno al menu principale.");
            return null;
        }
    }

    public static User login() {
        IO.clearScreen();

        int attempts = 0;
        while (true) {
            if (attempts >= 4) throw new AbortOperationException("Raggiunto limite massimo di tentativi");

            String username = IO.getUserInput("Inserisci il nome utente:");
            String password = IO.getUserInput("Inserisci la password:");

            try {
                User user = Loader.getUsersByName().get(username);
                if (user == null)
                    throw new IllegalArgumentException("Utente non trovato");

                if (user.verifyPassword(password)) {
                    IO.printSuccessMessage("Login effettuato con successo!");
                    IO.getUserInput("Premi Invio per continuare.");
                    return user;
                } else {
                    IO.printErrorMessage("Username o password errati.");
                    IO.getUserInput("Premi Invio per riprovare.");
                    attempts++;
                }

            } catch (IllegalArgumentException ex) {
                IO.printErrorMessage(ex.getMessage());
                attempts++;
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
                return null;
            }
        }
    }
}
