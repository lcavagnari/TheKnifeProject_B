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

        boolean isOWner = IO.getBooleanInput("Benvenuto! sei un gestore o un cliente? [Si - Gestore, No - Cliente]: ");

        String username = null;
        while (username == null) {
            try {
                username = IO.getUserInput("Enter desired username (min 4 chars):").trim();
                if (Loader.getUsersByName().containsKey(username))
                    throw new IllegalArgumentException("Username non disponibile");

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", username);
            } catch (IllegalArgumentException ex) {
                username = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }


        String firstName = null;
        String lastName = null;
        while (firstName == null || lastName == null) {
            try {
                firstName = IO.getUserInput("Enter desired username (min 4 chars):").trim();
                lastName = IO.getUserInput("Enter desired username (min 4 chars):").trim();

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", firstName);
                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", lastName);

            } catch (IllegalArgumentException ex) {
                firstName = null;
                lastName = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }


        Location location = IO.getLocationInput(true);


        LocalDate dateOfBirth = null;
        while (dateOfBirth == null) {
            try {
                dateOfBirth = LocalDate.parse(
                        IO.getUserInput("Inserisci la tua data di nascita (YYYY-MM-DD)")
                );

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
                password = IO.getUserInput("Enter desired username (min 4 chars):").trim();
                if (Loader.getUsersByName().containsKey(username))
                    throw new IllegalArgumentException("Username non disponibile");

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", username);
                break;
            } catch (IllegalArgumentException ex) {
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }

        try {
            User user = (isOWner) ? new Owner(username, password, firstName, lastName, location, dateOfBirth) : new Client(username, password, firstName, lastName, location, dateOfBirth);

            if (IO.validateUser(user))
                IO.getUserInput("Registration successful! Press Enter to return to main menu.");

            Loader.getUsersByName().put(username,user);
            Loader.getUsersById().put(user.getId(),user);
            user.save();

            return user;

        } catch (IllegalArgumentException ex) {
            IO.printErrorMessage(ex.getMessage()+ " ritorno al menu principale");
            return null;
        }
    }


    public static User login() {
        IO.clearScreen();

        int attempts = 0;
        while(true) {
            String username = IO.getUserInput("Enter username:");
            String password = IO.getUserInput("Enter password:");

            if (attempts >= 4) throw new AbortOperationException("raggiunnto limite tentativi");

            try {
                // Placeholder for user authentication logic
                User user = Loader.getUsersByName().get(username);
                if (user == null)
                    throw new IllegalArgumentException("Impossiibile reperire user con tale nome");

                if (user.verifyPassword(password)) {
                    IO.printSuccessrMessage("Login successful!");
                    IO.getUserInput("Press Enter to continue.");
                    return user;

                } else {
                    IO.printErrorMessage("Utente o password errati.");
                    IO.getUserInput("Premere enter");
                    attempts++;
                }
            } catch (IllegalArgumentException ex) {
                IO.printErrorMessage(ex.getMessage());

            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }
    }
}

