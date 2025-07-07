package it.uninsubria.laboratorioa.ui;

import lombok.experimental.UtilityClass;


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
    /*
    private static void register() {
        IO.clearScreen();
        System.out.println("=== User Registration ===");

        String username;
        while (true) {
            username = IO.getUserInput("Enter desired username (min 4 chars):").trim();
            if (username.length() < 4) {
                IO.printErrorMessage("Username too short. Must be at least 4 characters.");
                continue;
            }
            if (usernameExists(username)) {
                IO.printErrorMessage("Username already exists. Choose another.");
                continue;
            }
            break;
        }

        String password;
        while (true) {
            password = IO.getUserInput("Enter password (min 6 chars):");
            String confirm = IO.getUserInput("Confirm password:");
            if (password.length() < 6) {
                IO.printErrorMessage("Password too short. Must be at least 6 characters.");
                continue;
            }
            if (!password.equals(confirm)) {
                IO.printErrorMessage("Passwords do not match.");
                continue;
            }
            break;
        }

        String name = IO.getUserInput("Enter your first name:");
        String lastName = IO.getUserInput("Enter your last name:");

        LocalDate dob = null;
        String dobInput = IO.getUserInput("Enter date of birth (YYYY-MM-DD) or leave blank:");
        if (!dobInput.isBlank()) {
            try {
                dob = LocalDate.parse(dobInput);
            } catch (Exception e) {
                IO.printErrorMessage("Invalid date format. Date of birth left empty.");
            }
        }

        RegisteredUser newUser = createRegisteredUser(username, password, name, lastName, dob);
        saveUser(newUser);

        IO.getUserInput("Registration successful! Press Enter to return to main menu.");
    }

     */
}

