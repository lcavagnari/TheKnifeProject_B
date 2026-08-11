package it.uninsubria.laboratoriob.ui.menus;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.data.CustomerDAO;
import it.uninsubria.laboratoriob.data.OwnerDAO;
import it.uninsubria.laboratoriob.data.UserDAO;
import it.uninsubria.laboratoriob.ui.IO;
import it.uninsubria.laboratoriob.utils.Loader;
import it.uninsubria.laboratoriob.utils.PasswordHasher;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe di utility per la gestione del login e della registrazione utenti.
 * <p>
 * Fornisce metodi statici per facilitare le operazioni di autenticazione e
 * creazione di utenti nel sistema (customeri o gestori). I metodi interagiscono
 * con la console tramite {@link IO} e con lo storage in-memory/file tramite {@link Loader}.
 * </p>
 *
 * <h2>Side effects</h2>
 * <ul>
 *   <li><b>register()</b>: inserisce l'utente creato nelle mappe di {@link Loader}
 *       (username→utente e id→utente) e invoca {@link UserDAO#save(User)}.</li>
 *   <li><b>login()</b>: nessuna modifica allo stato persistente, ma può lanciare
 *       {@link AbortOperationException} se superato il numero massimo di tentativi.</li>
 * </ul>
 *
 * <p>I metodi possono propagare eccezioni di validazione come {@link IllegalArgumentException}
 * originate dai metodi di {@link IO} o da controlli locali.</p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
@UtilityClass
public class LoginMenu {

    private static final CustomerDAO CUSTOMER_DAO = new CustomerDAO();
    private static final OwnerDAO OWNER_DAO = new OwnerDAO();

    /**
     * Avvia una procedura guidata di registrazione utente.
     * <p>
     * Passi principali:
     * <ol>
     *   <li>Scelta del tipo utente: gestore ({@link Owner}) o customere ({@link Customer}).</li>
     *   <li>Raccolta e validazione di username, nome, cognome, location (facoltativa) e data di nascita.</li>
     *   <li>Impostazione e validazione della password.</li>
     *   <li>Creazione dell’istanza {@link User}, salvataggio su {@link Loader} e persistenza via {@link UserDAO#save(User)}.</li>
     * </ol>
     * </p>
     *
     * <h3>Input/Output</h3>
     * Interagisce con l’utente via console usando i metodi di {@link IO}. L’utente può annullare
     * in vari passaggi usando il comando previsto da {@link AbortOperationException#getCANCEL_COMMAND()}.
     *
     * @return l’utente creato e registrato; {@code null} se l’operazione viene annullata o fallisce la validazione
     * @throws AbortOperationException  se l’utente richiede esplicitamente l’annullamento in uno dei prompt obbligatori
     *                                  (l’eccezione può essere intercettata internamente e tradotta in {@code null}
     *                                  in base al punto della procedura)
     * @throws IllegalArgumentException se alcuni campi non rispettano i vincoli di validazione
     *                                  (può essere intercettata e trasformata in ri-prompt; in caso di errore finale
     *                                  viene mostrato un messaggio e restituito {@code null})
     */
    public static User register() {
        IO.clearScreen();
        System.out.println("=== User Registration ===");

        boolean isOwner = IO.getBooleanInput("Benvenuto! Sei un gestore o un cliente? [Sì - Gestore, No - Cliente]:");

        String username = null;
        while (username == null) {
            try {
                username = IO.getUserInput("Scegli un nome utente [4-16 caratteri]:").trim();
                if (Loader.hasUserByName(username))
                    throw new IllegalArgumentException("Username non disponibile");

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
            // Inserimento location opzionale, si prosegue con null
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
                    ? new Owner(username, password, PasswordHasher.generateSalt(), firstName, lastName, location, dateOfBirth)
                    : new Customer(username, password, PasswordHasher.generateSalt(), firstName, lastName, location, dateOfBirth);

            if (Validators.validateUser(user))
                IO.getUserInput("Registrazione completata! Premi Invio per tornare al menu principale.");

            Loader.addUser(user);

            if (isOwner) OWNER_DAO.save((Owner) user);
            else CUSTOMER_DAO.save((Customer) user);

            return user;

        } catch (IllegalArgumentException ex) {
            IO.printErrorMessage(ex.getMessage() + ". Ritorno al menu principale.");
            return null;
        }
    }

    /**
     * Esegue la procedura di autenticazione tramite username e password.
     * <p>
     * La funzione consente fino a 4 tentativi complessivi. In caso di credenziali errate
     * viene mostrato un messaggio e viene richiesto un nuovo tentativo; al superamento
     * del limite viene sollevata {@link AbortOperationException}.
     * </p>
     *
     * <h3>Flusso</h3>
     * <ol>
     *   <li>Richiesta username e password.</li>
     *   <li>Verifica esistenza utente in {@link Loader#getAllUsersByName()}.</li>
     *   <li>Verifica password tramite }.</li>
     *   <li>In caso di successo: messaggio, conferma su Invio, ritorno dell’utente.</li>
     * </ol>
     *
     * @return l’utente autenticato; {@code null} se l’operazione viene annullata dall’utente durante i prompt
     * @throws AbortOperationException  se viene superato il numero massimo di tentativi (4)
     * @throws IllegalArgumentException se l’username non esiste o altri controlli falliscono
     *                                  (l’eccezione è gestita internamente per riprovare, ma può emergere in casi non previsti)
     */
    public static User login() {
        IO.clearScreen();

        int attempts = 0;
        try {
            while (attempts < 4) {
                if (attempts >= 4) throw new AbortOperationException("Raggiunto limite massimo di tentativi");

                String username = IO.getUserInput("Inserisci il nome utente:");
                String password = IO.getUserInput("Inserisci la password:");

                User user = Loader.findUserByName(username);
                if (user == null || Loader.isSystemUser(user))
                    IO.printErrorMessage("Utente non trovato");

                else if (PasswordHasher.verify(password, user.getPasswordSalt(), user.getPasswordHash())) {
                    IO.printSuccessMessage("Login effettuato con successo!");
                    IO.getUserInput("Premi Invio per continuare.");
                    return user;

                } else {
                    IO.printErrorMessage("Username o password errati.");
                    IO.getUserInput("Premi Invio per riprovare.");
                    attempts++;
                }
            }

        } catch (IllegalArgumentException ex) {
            IO.printErrorMessage(ex.getMessage());
        } catch (AbortOperationException e) {
            IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
        }

        return null;
    }
}
