package it.uninsubria.laboratorioa.ui;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Classe di utilità per Input/Output da console.<p>
 * Contiene metodi statici per leggere input da tastiera, formattare output e gestire menu testuali.<p>
 * Utilizza Scanner per lettura standard input.<p>
 * Supporta formati booleani personalizzati, gestione menu, messaggi di errore e pulizia schermo.
 * <p>
 * @author Luca Cavagnari
 * @version 1.0
 */
public class IO {

    /**
     * Istanza Scanner condivisa per lettura input da tastiera.
     */
    private static final Scanner INPUT = new Scanner(System.in);

    /**
     * Chiude lo Scanner per input.
     */
    public static void closeScanner() {
        INPUT.close();
    }

    /**
     * Formatta un valore booleano in "Yes" o "No".
     *
     * @param input variabile booleana da formattare
     * @return "Yes" se true, "No" se false
     */
    public static String formatBoolean(boolean input) {
        return String.valueOf(input).replace("true", "Sì").replace("false", "No");
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
            if (items[i] != null) System.out.println((i + 1) + " - " + items[i]);

        if (footer != null) System.out.println("\n" + footer + "\n");
        else System.out.println();
    }

    /**
     * Richiede input booleano yes/no all'utente con validazione.
     *
     * @param promptMessage Messaggio di richiesta input
     * @return true se input valido "yes" o "y", false se "no" o "n"
     */
    public static boolean getBooleanInput(String promptMessage) {
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
    public static String getUserInput(String promptMessage) {
        System.out.print("\n" + IO.replaceText(32, "> ") + promptMessage);
        while (!INPUT.hasNext()) INPUT.nextLine();

        System.out.println();
        return INPUT.nextLine();
    }

    /**
     * Richiede input testuale all'utente.
     *
     * @param promptMessage Messaggio di richiesta input
     * @return Stringa inserita dall'utente
     */
    public static String getPhoneNumber(String promptMessage) {
        String input = getUserInput(promptMessage,10,15);

        while (!input.matches("\\+\\d{13,15}")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput(promptMessage,10,15);
        }

        return input;
    }


    /**
     * Richiede input testuale all'utente con controllo lunghezza minima e massima.
     *
     * @param promptMessage Messaggio di richiesta input
     * @param minLength     Lunghezza minima accettata
     * @param maxLength     Lunghezza massima accettata
     * @return Stringa inserita conforme ai limiti di lunghezza
     */
    public static String getUserInput(String promptMessage, int minLength, int maxLength) {
        String input;
        do {
            input = getUserInput(promptMessage);
        } while (input.length() < minLength || input.length() > maxLength);

        return input;
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
     * Richiede input numerico intero con validità limitata per menu (0..max).
     *
     * @param prompt Messaggio di richiesta input
     * @param max    Valore massimo valido
     * @return Intero valido nell'intervallo [0..max]
     */
    public static int getInt(String prompt, int max) {
        int sel = getInt(prompt);
        while (sel < 0 || sel >= max + 1) {
            IO.printErrorMessage("Valore non valido");
            sel = getInt(prompt);
        }

        return Math.max(sel - 1, 0);
    }


    public static Award parseAward(String input) {
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("Valore Award nullo o vuoto.");
        try {
            return Award.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valore Award non valido: " + input);
        }
    }

    public static CuisineType parseCuisineType(String input) {
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("Valore CuisineType nullo o vuoto.");
        try {
            return CuisineType.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valore CuisineType non valido: " + input);
        }
    }

    public static PriceRange parsePriceRange(String input) {
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("Valore PriceRange nullo o vuoto.");
        try {
            return PriceRange.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valore PriceRange non valido: " + input);
        }
    }

    // Sostituisci Enum4 con il quarto enum effettivo
    public static Nation parseNation(String input) {
        if (input == null || input.isBlank())
            throw new IllegalArgumentException("Valore Enum4 nullo o vuoto.");
        try {
            return Enum4.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valore Enum4 non valido: " + input);
        }
    }


    // Validators

    public static boolean validateString(String value, final String regex)  {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Il valore inserito non può essere vuoto.");
        if (!value.matches(regex))
            throw new IllegalArgumentException("Il valore inserito contiene caratteri non validi o ha lunghezza non consentita (4-30).");

        return true;
    }

    public static boolean validateString(String value, final String regex, String invalidValueErrorMessage)  {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Il valore inserito non può essere vuoto.");
        if (!value.matches(regex))
            throw new IllegalArgumentException(invalidValueErrorMessage);

        return true;
    }


    public static boolean validateLocation(Location loc) {
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
     * Verifica una o più date per non nullità e correttezza temporale (nessuna data futura consentita).
     * Solleva IllegalArgumentException con messaggi precisi in caso di violazione.
     *
     * @param dates Varargs di LocalDateTime da validare
     * @return true se tutte le date sono valide
     * @throws IllegalArgumentException se una data è nulla o futura
     */
    public static boolean validateDates(LocalDate max, LocalDate... dates) {
        return validateDates(max,dates);
    }

    /**
     * Validates one or more dates for non-nullity and temporal correctness (no future dates).
     * Throws IllegalArgumentException with precise message upon violation.
     *
     * @param dates Varargs LocalDateTime[] dates to validate
     * @return true if all dates are valid
     * @throws IllegalArgumentException if:
     *    - input array is null or empty
     *    - any date is null
     *    - any date is in the future compared to now
     */
    public static boolean validateDates(LocalDateTime max,LocalDateTime... dates) {
        if (dates == null || dates.length == 0) {
            throw new IllegalArgumentException("Impossibile verificare data, riprovare più tardi");
        }

        for (LocalDateTime date : dates) {
            if (date == null)
                throw new IllegalArgumentException("Impossibile determinare data, riprovare");

            if (date.isAfter(max) || date.isBefore(LocalDateTime.MIN))
                throw new IllegalArgumentException("Data fuori da intervalli accettabili ("+max.toString()+"), riprovare");
        }

        return true;
    }

    public static void validateReview(Review r) {
        if (r == null)
            throw new IllegalArgumentException("Recensione non può essere nulla.");


        if (r.getValue() < 1 || r.getValue() > 5)
            throw new IllegalArgumentException("Valutazione deve essere compresa tra 1 e 5.");

        validateString(r.getText(),"^[\\p{L}0-9 \\-']{4,200}$");

        if (r.date == null)
            throw new IllegalArgumentException("Data recensione non può essere nulla.");

        if (r.date.isAfter(java.time.LocalDate.now()))
            throw new IllegalArgumentException("Data recensione non può essere futura.");
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
