package it.uninsubria.laboratorioa.ui;


import java.util.Scanner;

public class IO {

    // Istanza scanner
    private static final Scanner INPUT = new Scanner(System.in);

    public static void closeScanner() {
        INPUT.close();
    }

    /**
     * Formatta un booleano in Si/No, al posto di true/false
     *
     * @param input variabile booleana
     * @return Si se true, No se false
     */
    public static String formatBoolean(boolean input) {
        return String.valueOf(input).replace("true", "Yes").replace("false", "No");
    }

    public static void printMenu(String title, String footer, String[] items) {
        if (items.length == 0) return;
        else if (title != null) System.out.println(title + "\n");

        for (int i = 0; i < items.length; i++)
            if (items[i] != null) System.out.println((i + 1) + " - " + items[i]);

        if (footer != null) System.out.println("\n" + footer + "\n");
        else System.out.println();
    }

    /**
     * Chiede un input al giocatore (yes/no)
     *
     * @param promptMessage Richiesta di input
     * @return Input in formato boolean
     */
    public static boolean getBooleanInput(String promptMessage) {
        String input = getUserInput(promptMessage).toLowerCase();

        // Usa un pattern di regex per controllare se input sia "yes" o "no"
        while (!input.matches("\\b(yes|no)\\b|\\b[yn]\\b")) {
            printErrorMessage("Invalid input, try again: ");
            input = getUserInput(promptMessage).toLowerCase();
        }

        // Sostituisce y oppure yes con True
        String bool = input.replaceAll("\\by.{2}\\b|y", "True");
        return Boolean.parseBoolean(bool);
    }

    /**
     * Chiede un input al giocatore (testo)
     *
     * @param promptMessage Richiesta di input
     * @return Input in formato stringa
     */
    public static String getUserInput(String promptMessage) {
        System.out.print("\n" + IO.replaceText(32, "> ") + promptMessage);
        while (!INPUT.hasNext()) INPUT.nextLine();

        System.out.println();
        return INPUT.nextLine();
    }


    /**
     * Chiede un input al giocatore (numero non decimale)
     *
     * @param promptMessage Richiesta di input
     * @return Input in formato integer
     */
    public static Integer getInt(String promptMessage) {
        String input = getUserInput(promptMessage);

        while (!input.matches("^-?\\d+$")) {
            IO.printErrorMessage("Invalid input");
            input = getUserInput(promptMessage);
        }

        return Integer.parseInt(input);
    }

    /**
     * Simile al getInt nella classe IO, ma tarato specificamente per array e liste
     * I numeri validi vanno da 0 -> max+1 (usato principalmente nei menu, 0 usato per annullare)
     *
     * @param prompt Richiesta di input
     * @param max    Numero massimo considerato valido (generalmente la dimensione della lista o array)
     * @return Numero intero valido (fra 0 e max+1)
     */
    public static int getInt(String prompt, int max) {
        int sel = getInt(prompt);
        while (sel < 0 || sel >= max + 1)
            sel = getInt("Invalid, " + prompt);

        return sel - 1;
    }

    // Utilities


    /**
     * Formatta del testo usando il codice colore ANSI<br>
     * <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797"> Documentazione su github.com</a>
     *
     * @param colorCode Codice colore
     * @param text      Testo da formattare
     * @return Testo formattato
     */
    public static String replaceText(int colorCode, String text) {
        return "\u001B[" + colorCode + "m" + text + "\u001B[0m";
    }


    /**
     * Stampa il separatore di riga per una tabella, composto da "-"
     * <br><br>Formula: (qta * 4) +1
     *
     * @param qta numero di "-" da stampare
     */
    private static String printRow(int qta) {
        return "-".repeat(Math.max(0, (qta * 4) + 1));
    }


    // Messages

    /**
     * Stampa un messaggio di errore, già formattato
     *
     * @param msg Messaggio di errore da formattare e stampare
     */
    public static void printErrorMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(31, "! ") + msg);
    }

    /**
     * Stampa un messaggio di errore, già formattato
     *
     * @param msg Messaggio di errore da formattare e stampare
     */
    public static void printSuccessrMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(36, "! ") + msg);
    }


    /**
     * Ripulisce lo schermo utilizzando comandi di sistema o caratteri ANSI
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
            System.out.println("Unable to clear screen.");
        }
    }
}
