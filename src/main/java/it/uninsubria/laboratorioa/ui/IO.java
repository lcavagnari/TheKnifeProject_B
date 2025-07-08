package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Classe di utilità per Input/Output da console.<p>
 * Contiene metodi statici per leggere input da tastiera, formattare output e gestire menu testuali.<p>
 * Utilizza Scanner per lettura standard input.<p>
 * Supporta formati booleani personalizzati, gestione menu, messaggi di errore e pulizia schermo.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
public class IO {

    /**
     * Istanza Scanner condivisa per lettura input da tastiera.
     */
    private static final Scanner INPUT = new Scanner(System.in);
    private static final Random rd = new Random();

    /**
     * Chiude lo Scanner per input.
     */
    public static void closeScanner() {
        INPUT.close();
    }

    /**
     * Stampa un menu testuale con titolo, lista voci e footer opzionali.
     *
     * @param title  Titolo del menu, può essere null
     * @param footer Testo footer opzionale, può essere null
     * @param items  Array di voci menu da stampare
     */
    public static void printMenu(String title, String footer, String... items) {
        if (items.length == 0) return;
        else if (title != null) System.out.println(title + "\n");

        for (int i = 0; i < items.length; i++)
            if (items[i] != null || Objects.equals(items[i], "")) System.out.println("│ " + (i + 1) + " - " + items[i]);

        if (footer != null) System.out.println("\n" + footer + "\n");
        else System.out.println();
    }

    /**
     * Richiede input booleano yes/no all'utente con validazione.
     *
     * @param promptMessage Messaggio di richiesta input
     * @return true se input valido "yes" o "y", false se "no" o "n"
     */
    public static boolean getBooleanInput(String promptMessage) throws AbortOperationException {
        String input = getUserInput(promptMessage).toLowerCase();

        // Usa regex per controllare se input sia "sì" o "no", o "s" / "n"
        while (!input.matches("\\b(s[iì]|no)\\b|\\b[sn]\\b")) {
            printErrorMessage("Valore non valido, riprova: ");
            input = getUserInput(promptMessage).toLowerCase();
        }

        // Sostituisce s oppure sì con True
        String bool = input.replaceAll("\\bs.{1,2}\\b|s", "True");
        return Boolean.parseBoolean(bool);
    }


    /**
     * Richiede input testuale all'utente.
     *
     * @param promptMessage Messaggio di richiesta input
     * @return Stringa inserita dall'utente
     */
    public static String getMenuUserInput(String promptMessage) {
        INPUT.nextLine();
        System.out.print(IO.replaceText(32, "> ") + promptMessage);
        while (!INPUT.hasNext()) INPUT.nextLine();

        System.out.println();
        return INPUT.nextLine();
    }

    /**
     * Richiede input testuale all'utente.
     * Possibilità di annullare l'operazione con '::annulla'
     *
     * @param promptMessage Messaggio di richiesta input
     * @return Stringa inserita dall'utente
     */
    public static String getUserInput(String promptMessage) throws AbortOperationException {
        System.out.print(IO.replaceText(32, "> ") + promptMessage);
        while (!INPUT.hasNext()) INPUT.nextLine();

        String input = INPUT.nextLine();
        if (input.equals("::annulla")) throw new AbortOperationException("ricevuto comando di interruzione da utente");

        System.out.println();
        return input;
    }


    /**
     * Richiede input testuale all'utente con controllo lunghezza minima e massima.
     * Possibilità di annullare l'operazione con '::annulla'
     *
     * @param promptMessage Messaggio di richiesta input
     * @param minLength     Lunghezza minima accettata
     * @param maxLength     Lunghezza massima accettata
     * @return Stringa inserita conforme ai limiti di lunghezza
     */
    public static String getUserInput(String promptMessage, int minLength, int maxLength) throws AbortOperationException {
        String input;
        do {
            input = getUserInput(promptMessage);
        } while (input.length() < minLength || input.length() > maxLength);

        return input;
    }

    /**
     * Richiede input testuale all'utente.
     *
     * @return Stringa inserita dall'utente
     */
    public static String getPhoneNumberInput() throws AbortOperationException {
        String input = getUserInput("Inserire numero di telefono con prefisso nazionale:", 10, 15);

        while (!input.matches("\\+\\d{13,15}")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput("Inserire numero di telefono con prefisso nazionale:", 10, 15);
        }

        return input;
    }


    /**
     * Richiede input numerico intero non decimale all'utente.
     *
     * @param promptMessage Messaggio di richiesta input
     * @return Integer corrispondente all'input valido
     */
    public static Integer getMenuInt(String promptMessage) {
        String input = getMenuUserInput(promptMessage);

        while (!input.matches("^-?\\d+$")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput(promptMessage);
        }

        return Integer.parseInt(input);
    }

    /**
     * Richiede input numerico intero non decimale all'utente.
     *
     * @param promptMessage Messaggio di richiesta input
     * @return Integer corrispondente all'input valido
     */
    public static Integer getInt(String promptMessage) {
        String input = getUserInput(promptMessage);

        while (!input.matches("^-?\\d+$")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput(promptMessage);
        }

        return Integer.parseInt(input);
    }


    /**
     * Ottiene la posizione geografica di un entità tramite input utente
     *
     * @param skippable
     * @return
     * @throws AbortOperationException
     */
    public static Location getLocationInput(boolean skippable) throws AbortOperationException {
        Location location = null;
        System.out.println("Adesso puoi insrire l'indirizzo, ricodati di seguire il seguente template");
        System.out.println("Indirizzo: indirizzo completo di numero civico, senza città o nazione");
        System.out.println("Nazione: Nome completo della nazione");
        System.out.println("Esempio: Via Ottorino Rossi 9, Bizzozero, Italia");
        System.out.println("Città: Nome delle città");
        if (skippable) IO.printErrorMessage("\nDigitare '::skip' per saltare questo passaggio.\n");

        while (location == null) {
            try {

                // indirizzo, città, nazione
                String addrees = IO.getUserInput("Inserisci l'indirizzo nel seguente formato [indirizzo, città, nazione]: ").trim();
                if (addrees.equals("::skip") && skippable) throw new AbortOperationException();

                String[] fields = addrees.split(",");
                if (fields == null || fields.length < 3)
                    throw new IllegalArgumentException("input non valido");

                // Validazione Città
                IO.validateString(fields[1]);

                // Validazione indirizzo
                IO.validateString("^[\\p{L}][\\p{L}'\\-. ]+\\s\\d+[a-zA-Z]?$", fields[0]);

                location = new Location(
                        Nation.valueOf(fields[2].trim().toUpperCase().replace(" ", "_")),
                        fields[0],
                        -90 + (180 * rd.nextDouble()),
                        -180 + (360 * rd.nextDouble()),
                        fields[1]
                );
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                location = null;
                IO.printErrorMessage(ex.getMessage()+"\n");
            } catch (AbortOperationException e) {
                if (e.getMessage() != null)
                    IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return null;
            }
        }

        return location;
    }

    /**
     * Richiede e interpreta un valore Enum da input utente.<p>
     * Valori inseriti vengono normalizzati in MAIUSCOLO e con spazi sostituiti da underscore.
     *
     * @param enumType      Classe dell'enum da parsare
     * @param promptMessage Messaggio di richiesta all'utente
     * @param <T>           Tipo Enum da restituire
     * @return Istanza Enum parsata da input
     * @throws IllegalArgumentException se il valore inserito non è tra quelli accettati
     */
    public static <T extends Enum<T>> T getEnumInput(Class<T> enumType, String promptMessage) {
        while (true) {
            try {
                String input = getUserInput(promptMessage);
                return Enum.valueOf(enumType, input.trim().toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                IO.printErrorMessage("Valore non valido, riprovare");

            } catch (AbortOperationException e) {
                IO.printErrorMessage("Abort: " + e.getMessage());
                return null;
            }
        }
    }


    public static <E extends Enum<E>> Set<E> getEnumSetInput(Class<E> enumClass, String prompt) {
        Set<E> result = new HashSet<>();
        System.out.println(prompt + " Scrivi '::stop' per terminare.");

        while (true) {
            try {
                String input = IO.getUserInput("Inserisci nuovo valore:");
                if (input.equalsIgnoreCase("::stop")) break;

                E enumValue = Enum.valueOf(enumClass, input.trim().toUpperCase().replace(" ", "_"));
                result.add(enumValue);

            } catch (IllegalArgumentException e) {
                IO.printErrorMessage("Valore non valido. Riprova.");

            } catch (AbortOperationException e) {
                IO.printErrorMessage("Abort: " + e.getMessage());
                break;
            }
        }
        return result;
    }

    public static Set<String> parseValidatedStrings(String prompt) {
        Set<String> result = new HashSet<>();
        System.out.println(prompt + " Scrivi '::stop' per terminare.");

        while (true) {
            try {
                String input = IO.getUserInput("Valore:");
                if (input.equalsIgnoreCase("::stop")) break;

                IO.validateString(input);
                result.add(input.trim());

            } catch (IllegalArgumentException e) {
                IO.printErrorMessage("Input non valido. Riprova.");
            } catch (AbortOperationException e) {
                IO.printErrorMessage("Abort: " + e.getMessage());
                break;
            }
        }
        return result;
    }


    public static UUID parseUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }


    // Validators


    /**
     * Verifica uno o più valori stringa rispetto a un pattern regex, sollevando eccezioni in caso di input non valido.
     *
     * @param regex Pattern per la verifica
     * @param value Valori stringa da validare
     * @return true se tutti i valori sono validi
     * @throws IllegalArgumentException se uno o più valori sono null, vuoti o non corrispondenti alla regex
     */
    public static boolean validateString(final String regex, String value) throws IllegalArgumentException {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Il valore inserito non può essere vuoto.");
        if (!value.matches(regex))
            throw new IllegalArgumentException("Il valore inserito contiene caratteri non validi o ha un numero di caratteri non consentito (4-200).");
        return true;
    }

    public static boolean validateString(String value) throws IllegalArgumentException {
        return validateString("^[\\p{L}0-9 \\-']{4,200}$", value);
    }

    /**
     * Verifica l'oggetto Location e i suoi campi, verificandone non nullità e limiti geografici corretti.
     *
     * @param loc Oggetto Location da validare
     * @return true se la location è valida
     * @throws IllegalArgumentException se uno dei campi è nullo o fuori dai limiti accettati
     */
    public static boolean validateLocation(Location loc) throws IllegalArgumentException {
        if (loc == null)
            throw new IllegalArgumentException("Impossibile determinare posizione, riprovare più tardi");

        if (loc.getNation() == null)
            throw new IllegalArgumentException("Impossibile determinare nazione: input non valido");

        if (loc.getCity() == null || loc.getCity().isBlank())
            throw new IllegalArgumentException("Impossibile determinare città: input non valido");

        if (loc.getAddress() == null || loc.getAddress().isBlank())
            throw new IllegalArgumentException("Impossibile determinare indirizzo: input non valido");

        if (loc.getLatitude() < -90.0 || loc.getLatitude() > 90.0)
            throw new IllegalArgumentException("Impossibile determinare latitudine: input fuori da valori accettabili (-90°,+90°).");

        if (loc.getLongitude() < -180.0 || loc.getLongitude() > 180.0)
            throw new IllegalArgumentException("Impossibile determinare longitudine: input fuori da valori accettabili (-180°,+180°)");

        return true;
    }


    /**
     * Verifica una data, verificando non nullità e che rientri in un intervallo temporale specificato.
     *
     * @param max  Data massima accettabile (inclusa)
     * @param min  Data minima accettabile (inclusa)
     * @param date Data da validare
     * @return true se la data è valida
     * @throws IllegalArgumentException se la data è nulla o fuori dall'intervallo [min, max]
     */
    public static boolean validateDates(LocalDateTime min, LocalDateTime max, LocalDateTime date) throws IllegalArgumentException {
        if (date == null)
            throw new IllegalArgumentException("Impossibile verificare data, riprovare più tardi");

        if (date.isAfter(max) || date.isBefore(min))
            throw new IllegalArgumentException("Data fuori da intervalli accettabili (" + max.toString() + "), riprovare");

        return true;
    }

    public static boolean validateDates(LocalDateTime date) throws IllegalArgumentException {
        return validateDates(LocalDateTime.MIN, LocalDateTime.MAX, date);
    }

    /**
     * Verifica la validità dell'id
     *
     * @param id
     * @return
     */
    public static boolean validateUUID(UUID id) throws IllegalArgumentException {
        if (id == null)
            throw new IllegalArgumentException("Impossibile ottenere id utente, riprovare più tardi");

        return true;
    }

    /**
     * Validates one or more dates for non-nullity and temporal correctness (no future dates).
     * Throws IllegalArgumentException with precise message upon violation.
     *
     * @param r Varargs LocalDateTime[] dates to validate
     * @return true if all dates are valid
     * @throws IllegalArgumentException if:
     *                                  - input array is null or empty
     *                                  - any date is null
     *                                  - any date is in the future compared to now
     */
    public static boolean validateReview(Review r) throws IllegalArgumentException {
        if (r == null)
            throw new IllegalArgumentException("Recensione non può essere nulla.");


        if (r.getValue() < 1 || r.getValue() > 5)
            throw new IllegalArgumentException("Valutazione deve essere compresa tra 1 e 5.");

        if (r.getReply() != null && !r.getReply().matches("^[\\p{L}0-9 \\-']{4,200}$"))
            throw new IllegalArgumentException("Il valore inserito contiene caratteri non validi o ha un numero di caratteri non consentito (4-200).");

        validateUUID(r.getId());
        validateString("^[\\p{L}0-9 \\-']{4,200}$", r.getText());
        validateDates(r.getTimestamp());
        return false;
    }


    /**
     * Verifica un utente controllando i campi obbligatori e formati corretti.
     *
     * @param user Utente da validare
     * @return true se l'utente è valido
     * @throws IllegalArgumentException se l'utente è nullo o i campi non rispettano i vincoli
     */
    public static boolean validateUser(User user) throws IllegalArgumentException {
        if (user == null)
            throw new IllegalArgumentException("Impossibile ottenere utente, riprovare più tardi");

        validateUUID(user.getId());

        validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", user.getName());
        validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", user.getName());

        validateString("^[a-zA-Z][\\w.]{1,14}[a-zA-Z0-9]$", user.getUsername());

        if (user.getLocation() != null) validateLocation(user.getLocation());

        validateDates(LocalDateTime.MIN, LocalDateTime.now().plusDays(1), user.getDateOfBirth().atStartOfDay());

        return true;
    }


    public static boolean validateRestaurant(Restaurant r) throws IllegalArgumentException {
        if (r == null)
            throw new IllegalArgumentException("Impossibile ottenere dati ristorante, riprovare più tardi");

        validateUUID(r.getId());
        validateString("^[\\p{L}][\\p{L}'\\- ]{1,40}$", r.getName());
        validateString(r.getDescription());

        validateString("^(https?://)?[\\w.-]+(\\.[a-z]{2,})+.*$", r.getWebsiteUrl());
        validateString("^\\+\\d{8,15}$", r.getPhone());

        validateUser(r.getOwner());

        return true;
    }

    /**
     * Applica formattazione ANSI colore a testo.
     *
     * @param colorCode Codice colore ANSI
     * @param text      Testo da formattare
     * @return Testo colorato con codice ANSI
     */
    public static String replaceText(int colorCode, String text) {
        return "\u001B[" + colorCode + "m" + text + "\u001B[0m";
    }

    /**
     * Genera una linea separatrice composta da '-' per tabelle.
     *
     * @param qta Numero di elementi della tabella da separare
     * @return Stringa composta da "-" della lunghezza calcolata
     */
    private static String printRow(int qta) {
        return "-".repeat(Math.max(0, (qta * 4) + 1));
    }

    /**
     * Stampa messaggio di errore formattato in rosso.
     *
     * @param msg Messaggio di errore
     */
    public static void printErrorMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(31, "! ") + msg);
    }

    /**
     * Stampa messaggio di successo formattato in verde.
     *
     * @param msg Messaggio di successo
     */
    public static void printSuccessrMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(32, "! ") + msg);
    }

    /**
     * Pulisce la schermata console usando comandi di sistema o sequenze ANSI.
     */
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Impossibile pulire lo schermo.");
        }
    }
}
