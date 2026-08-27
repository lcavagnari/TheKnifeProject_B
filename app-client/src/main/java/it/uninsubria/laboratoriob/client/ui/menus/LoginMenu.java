package it.uninsubria.laboratoriob.client.ui.menus;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import lombok.experimental.UtilityClass;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe di utility per la gestione del login e della registrazione utenti lato client.
 */
@UtilityClass
public class LoginMenu {

    private static boolean isSystemUser(User user) {
        return user != null && user.isSystem();
    }

    public static User register(ClientDataStore dataStore) {
        IO.clearScreen();
        System.out.println("=== User Registration ===");

        boolean isOwner = IO.getBooleanInput("Benvenuto! Sei un gestore o un cliente? [Si - Gestore, No - Cliente]:");

        String username = null;
        while (username == null) {
            try {
                username = IO.getUserInput("Scegli un nome utente [4-16 caratteri]:").trim();
                Validators.validateString("^[\\p{L}][\\p{L}'\\- ]{3,39}$", username);
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

                Validators.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", firstName);
                Validators.validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", lastName);
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
                Validators.validateDates(LocalDateTime.MIN, LocalDateTime.now(), dateOfBirth.atStartOfDay());
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
                Validators.validateString(".{4,}", password);
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
                    ? new Owner(username, "", "", firstName, lastName, location, dateOfBirth)
                    : new Customer(username, "", "", firstName, lastName, location, dateOfBirth);

            if (Validators.validateUser(user))
                IO.getUserInput("Registrazione completata! Premi un qualsiasi tasto + Invio per tornare al menu principale.");

            if (isOwner) dataStore.getOwnerDAO().save((Owner) user);
            else dataStore.getCustomerDAO().save((Customer) user);

            return user;

        } catch (IllegalArgumentException | UnsupportedOperationException ex) {
            IO.printErrorMessage(ex.getMessage() + ". Ritorno al menu principale.");
            return null;
        }
    }

    public static User login(ClientDataStore dataStore) {
        IO.clearScreen();

        int attempts = 0;
        try {
            while (attempts < 4) {
                String username = IO.getUserInput("Inserisci il nome utente:");
                String password = IO.getUserInput("Inserisci la password:");

                User user;

                if (dataStore.getAuthService() != null) {
                    try {
                        user = dataStore.getAuthService().loginCustomer(username, password);
                    } catch (RemoteException ignored) {
                        try {
                            user = dataStore.getAuthService().loginOwner(username, password);
                        } catch (RemoteException ex) {
                            throw new AbortOperationException(
                                    (ex.getMessage().isEmpty()) ? "Server non raggiungibile." : "Errore: "+ex.getMessage()
                            );
                        }
                    }
                } else {
                    IO.printErrorMessage("Servizio auth non disponibile.");
                    return null;
                }

                if (user == null || isSystemUser(user))
                    IO.printErrorMessage("Utente non trovato");
                else {
                    IO.printSuccessMessage("Login effettuato con successo!");
                    IO.getUserInput("Premi Invio per continuare.");
                    return user;
                }

                attempts++;
            }

            throw new AbortOperationException("Raggiunto limite massimo di tentativi");

        } catch (IllegalArgumentException ex) {
            IO.printErrorMessage(ex.getMessage());
        } catch (AbortOperationException e) {
            IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
        }

        return null;
    }
}
