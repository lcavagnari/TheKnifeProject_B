package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.users.Client;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.IO;
import it.uninsubria.laboratorioa.utils.Loader;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;


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

    private static final Random rd = new Random();

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


        String fisrtName = null;
        while (fisrtName == null) {
            try {
                fisrtName = IO.getUserInput("Enter desired username (min 4 chars):").trim();

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", fisrtName);
                break;
            } catch (IllegalArgumentException ex) {
                fisrtName = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }

        String lastName = null;
        while (lastName == null) {
            try {
                lastName = IO.getUserInput("Enter desired username (min 4 chars):").trim();

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", lastName);
            } catch (IllegalArgumentException ex) {
                lastName = null;
                IO.printErrorMessage(ex.getMessage());

            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }

        Location location = null;
        System.out.println("Adesso puoi insrire l'indirizzo, ricodati di seguire il seguente template");
        System.out.println("Indirizzo: indirizzo completo di numero civico, senza città o nazione");
        System.out.println("Città: Nome delle città");
        System.out.println("Nazione: Nome completo della nazione");
        System.out.println("Esempio: Via Ottorino Rossi 9, Bizzozero, Italia");

        while (location == null) {
            try {

                // indirizzo, città, nazione
                String addrees = IO.getUserInput("Inserisci l'indirizzo nel seguente formato [indirizzo, città, nazione]: ").trim();

                String[] fields = addrees.split(",");
                if (fields  == null || fields.length < 3)
                    throw new IllegalArgumentException("input non valido");

                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", fields[1]);
                IO.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", fields[0]);

                location = new Location(
                        Nation.valueOf(fields[2].trim().replace(" ","_")),
                        fields[0],
                        -90 + (180 * rd.nextDouble()),
                        -180 + (360 * rd.nextDouble()),
                        fields[1]
                );

            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                location = null;
                IO.printErrorMessage(ex.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage()+((e.getReason() != null) ? "Reason: "+e.getReason() : ""));
                return null;
            }
        }

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
            User user = (isOWner) ? new Owner(username, password, fisrtName, lastName, location, dateOfBirth) : new Client(username, password, fisrtName, lastName, location, dateOfBirth);

            if (IO.validateUser(user))
                IO.getUserInput("Registration successful! Press Enter to return to main menu.");

            user.save();

            return user;

        } catch (IllegalArgumentException ex) {
            IO.printErrorMessage(ex.getMessage()+ " ritorno al menu principale");
        }
    }


    public static User login() {
        IO.clearScreen();

        int attempts =0;
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

