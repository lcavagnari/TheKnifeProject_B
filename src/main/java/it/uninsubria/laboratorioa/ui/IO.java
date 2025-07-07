package it.uninsubria.laboratorioa.ui;

import java.util.Scanner;

/**
 * Classe di utilità per Input/Output da console.<p>
 * Contiene metodi statici per leggere input da tastiera, formattare output e gestire menu testuali.<p>
 * Utilizza Scanner per lettura standard input.<p>
 * Supporta formati booleani personalizzati, gestione menu, messaggi di errore e pulizia schermo.
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
    public static void printMenu(String title, String footer, String[] items) {
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
            printErrorMessage("Input non valido, riprova: ");
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
     * Richiede input testuale all'utente con controllo lunghezza minima e massima.
     *
     * @param promptMessage Messaggio di richiesta input
     * @param minLength     Lunghezza minima accettata
     * @param maxLength     Lunghezza massima accettata
     * @return Stringa inserita conforme ai limiti di lunghezza
     */
    public static String getUserInput(String promptMessage, int minLength, int maxLength) {
        String input = "";
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
            IO.printErrorMessage("Input non valido");
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
        while (sel < 0 || sel >= max + 1)
            sel = getInt("Input non valido, " + prompt);

        return sel - 1;
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
     * Stampa messaggio di successo formattato in ciano.
     *
     * @param msg Messaggio di successo
     */
    public static void printSuccessrMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(36, "! ") + msg);
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
