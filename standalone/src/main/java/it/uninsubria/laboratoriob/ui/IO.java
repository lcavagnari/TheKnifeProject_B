package it.uninsubria.laboratoriob.ui;

import it.uninsubria.laboratoriob.Validators;
import it.uninsubria.laboratoriob.enums.Nation;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.objects.User;
import it.uninsubria.laboratoriob.exceptions.AbortOperationException;

import java.util.*;

/**
 * Classe di utilità per Input/Output da console.<p>
 * Contiene metodi statici per leggere input da tastiera, formattare output e gestire menu testuali.<p>
 * Utilizza Scanner per la lettura dallo standard input.<p>
 * Supporta formati booleani personalizzati, gestione menu, messaggi di errore e pulizia schermo.
 *
 * @author Luca Cavagnari
 * @version 1.1
 */
public class IO {

    /** Istanza Scanner condivisa per la lettura da tastiera. */
    private static final Scanner INPUT = new Scanner(System.in);

    /** Generatore casuale utilizzato in alcune operazioni di input simulato. */
    private static final Random rd = new Random();

    /**
     * Chiude lo scanner condiviso per liberare le risorse di input.
     */
    public static void closeScanner() {
        INPUT.close();
    }

    /**
     * Stampa un menu testuale con titolo, voci numerate e footer opzionale.
     *
     * @param title  titolo del menu (può essere null)
     * @param footer testo footer opzionale (può essere null)
     * @param items  array di voci del menu da stampare
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
     * Richiede input booleano all’utente (sì/no) con validazione.
     *
     * @param promptMessage messaggio di richiesta input
     * @return true se l’input è "s" o "sì", false se "n" o "no"
     * @throws AbortOperationException se viene inserito il comando di annullamento
     */
    public static boolean getBooleanInput(String promptMessage) throws AbortOperationException {
        String input = getUserInput(promptMessage).toLowerCase();

        while (!input.matches("\\b(s[iì]|no)\\b|\\b[sn]\\b")) {
            printErrorMessage("Valore non valido, riprova: ");
            input = getUserInput(promptMessage).toLowerCase();
        }

        String bool = input.replaceAll("\\bs.{1,2}\\b|s", "True");
        return Boolean.parseBoolean(bool);
    }

    /**
     * Richiede un input testuale da menu.
     *
     * @param promptMessage messaggio di richiesta input
     * @return stringa inserita dall’utente
     */
    public static String getMenuUserInput(String promptMessage) {
        INPUT.nextLine();
        System.out.print(IO.replaceText(32, "> ") + promptMessage);
        while (!INPUT.hasNext()) INPUT.nextLine();
        System.out.println();
        return INPUT.nextLine();
    }

    /**
     * Richiede input testuale all’utente.
     * Permette di annullare l’operazione digitando ‘::annulla’.
     *
     * @param promptMessage messaggio di richiesta input
     * @return stringa inserita dall’utente
     * @throws AbortOperationException se viene richiesto l’annullamento
     */
    public static String getUserInput(String promptMessage) throws AbortOperationException {
        System.out.print(IO.replaceText(32, "> ") + promptMessage + " ");
        while (!INPUT.hasNext()) INPUT.nextLine();

        String input = INPUT.nextLine();
        if (input.equals(AbortOperationException.getCANCEL_COMMAND()))
            throw new AbortOperationException("ricevuto comando di interruzione da utente");

        System.out.println();
        return input;
    }

    /**
     * Richiede input testuale con controllo di lunghezza minima e massima.
     * Permette l’annullamento tramite ‘::annulla’.
     *
     * @param promptMessage messaggio di richiesta input
     * @param minLength     lunghezza minima accettata
     * @param maxLength     lunghezza massima accettata
     * @return stringa conforme ai limiti
     * @throws AbortOperationException se viene richiesto l’annullamento
     */
    public static String getUserInput(String promptMessage, int minLength, int maxLength) throws AbortOperationException {
        String input;
        do {
            input = getUserInput(promptMessage);
        } while (input.length() < minLength || input.length() > maxLength);

        return input;
    }

    /**
     * Richiede un numero di telefono valido con prefisso nazionale.
     *
     * @return numero di telefono in formato internazionale
     * @throws AbortOperationException se viene richiesto l’annullamento
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
     * Richiede input numerico intero (non decimale) all’utente per menu.
     *
     * @param promptMessage messaggio di richiesta input
     * @return valore intero valido
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
     * Richiede input numerico intero (non decimale) all’utente.
     *
     * @param promptMessage messaggio di richiesta input
     * @return valore intero valido
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
     * Ottiene la posizione geografica di un’entità tramite input utente.
     *
     * @param skippable true se è possibile saltare l’inserimento digitando ‘::skip’
     * @return istanza {@link Location} creata da input utente, oppure null se saltata
     * @throws AbortOperationException se l’utente interrompe l’operazione
     */
    public static Location getLocationInput(boolean skippable) throws AbortOperationException {
        Location location = null;
        System.out.println("Puoi inserire l'indirizzo seguendo il formato indicato:");
        System.out.println("Esempio: Via Ottorino Rossi 9, Bizzozero, Italia");
        if (skippable) IO.printErrorMessage("\nDigitare '::skip' per saltare questo passaggio.\n");

        while (location == null) {
            try {
                String addrees = IO.getUserInput("Inserisci l'indirizzo nel seguente formato [indirizzo, città, nazione]: ").trim();
                if (addrees.equals("::skip") && skippable) throw new AbortOperationException();

                String[] fields = addrees.split(",");
                if (fields == null || fields.length < 3)
                    throw new IllegalArgumentException("input non valido");

                Validators.validateString(fields[1]);
                Validators.validateString("^[\\p{L}][\\p{L}'\\-. ]+\\s\\d+[a-zA-Z]?$", fields[0]);

                location = new Location(
                        Nation.valueOf(fields[2].trim().toUpperCase().replace(" ", "_")),
                        fields[0],
                        -90 + (180 * rd.nextDouble()),
                        -180 + (360 * rd.nextDouble()),
                        fields[1]
                );
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                location = null;
                IO.printErrorMessage(ex.getMessage() + "\n");
            } catch (AbortOperationException e) {
                if (e.getMessage() != null)
                    IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? "Reason: " + e.getReason() : ""));
                return null;
            }
        }
        return location;
    }

    /**
     * Richiede e interpreta un valore Enum da input utente.<br>
     * L’input viene normalizzato in MAIUSCOLO e gli spazi sostituiti da underscore.
     *
     * @param enumType      classe dell’enum
     * @param promptMessage messaggio di richiesta
     * @param <T>           tipo Enum
     * @return valore Enum parsato da input, o null se interrotto
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

    /**
     * Permette l’inserimento di un insieme di valori Enum tramite input multiplo.
     *
     * @param enumClass classe dell’enum
     * @param prompt    messaggio iniziale
     * @param <E>       tipo Enum
     * @return set di valori Enum inseriti
     */
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

    /**
     * Permette di inserire e validare più stringhe, terminando con ‘::stop’.
     *
     * @param prompt messaggio iniziale
     * @return insieme di stringhe validate
     */
    public static Set<String> parseValidatedStrings(String prompt) {
        Set<String> result = new HashSet<>();
        System.out.println(prompt + " Scrivi '::stop' per terminare.");

        while (true) {
            try {
                String input = IO.getUserInput("Valore:");
                if (input.equalsIgnoreCase("::stop")) break;
                Validators.validateString(input);
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

    /**
     * Converte una stringa UUID in oggetto UUID.
     *
     * @param uuid stringa UUID
     * @return UUID valido o null se invalido
     */
    public static UUID parseUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }



    /**
     * Applica formattazione ANSI per il colore del testo.
     *
     * @param colorCode codice colore ANSI
     * @param text      testo da colorare
     * @return testo colorato
     */
    public static String replaceText(int colorCode, String text) {
        return "\u001B[" + colorCode + "m" + text + "\u001B[0m";
    }

    /**
     * Genera una riga separatrice composta da trattini.
     *
     * @param qta numero di elementi della tabella
     * @return stringa di separazione
     */
    private static String printRow(int qta) {
        return "-".repeat(Math.max(0, (qta * 4) + 1));
    }

    /**
     * Stampa un messaggio di errore colorato in rosso.
     *
     * @param msg messaggio di errore
     */
    public static void printErrorMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(31, "! ") + msg);
    }

    /**
     * Stampa un messaggio di successo colorato in verde.
     *
     * @param msg messaggio di successo
     */
    public static void printSuccessMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(32, " ⩥ ") + msg);
    }

    /**
     * Pulisce la schermata della console (Windows o Unix-like).
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
