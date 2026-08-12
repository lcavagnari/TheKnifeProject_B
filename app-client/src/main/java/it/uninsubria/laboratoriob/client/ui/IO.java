package it.uninsubria.laboratoriob.client.ui;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Location;

import java.util.*;

/**
 * Classe di utilita per Input/Output da console.
 * Contiene metodi statici per leggere input da tastiera, formattare output e gestire menu testuali.
 */
public class IO {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final Random rd = new Random();

    public static void closeScanner() {
        INPUT.close();
    }

    public static void printMenu(String title, String footer, String... items) {
        if (items.length == 0) return;
        else if (title != null) System.out.println(title + "\n");

        for (int i = 0; i < items.length; i++)
            if (items[i] != null || Objects.equals(items[i], "")) System.out.println("│ " + (i + 1) + " - " + items[i]);

        if (footer != null) System.out.println("\n" + footer + "\n");
        else System.out.println();
    }

    public static boolean getBooleanInput(String promptMessage) throws AbortOperationException {
        String input = getUserInput(promptMessage).toLowerCase();

        while (!input.matches("\\b(s[iì]|no)\\b|\\b[sn]\\b")) {
            printErrorMessage("Valore non valido, riprova: ");
            input = getUserInput(promptMessage).toLowerCase();
        }

        String bool = input.replaceAll("\\bs.{1,2}\\b|s", "True");
        return Boolean.parseBoolean(bool);
    }

    public static String getMenuUserInput(String promptMessage) {
        INPUT.nextLine();
        System.out.print(IO.replaceText(32, "> ") + promptMessage);
        while (!INPUT.hasNext()) INPUT.nextLine();
        System.out.println();
        return INPUT.nextLine();
    }

    public static String getUserInput(String promptMessage) throws AbortOperationException {
        System.out.print(IO.replaceText(32, "> ") + promptMessage + " ");
        while (!INPUT.hasNext()) INPUT.nextLine();

        String input = INPUT.nextLine();
        if (input.equals(AbortOperationException.getCANCEL_COMMAND()))
            throw new AbortOperationException("ricevuto comando di interruzione da utente");

        System.out.println();
        return input;
    }

    public static String getUserInput(String promptMessage, int minLength, int maxLength) throws AbortOperationException {
        String input;
        do {
            input = getUserInput(promptMessage);
        } while (input.length() < minLength || input.length() > maxLength);

        return input;
    }

    public static String getPhoneNumberInput() throws AbortOperationException {
        String input = getUserInput("Inserire numero di telefono con prefisso nazionale:", 10, 15);

        while (!input.matches("\\+\\d{13,15}")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput("Inserire numero di telefono con prefisso nazionale:", 10, 15);
        }

        return input;
    }

    public static Integer getMenuInt(String promptMessage) {
        String input = getMenuUserInput(promptMessage);

        while (!input.matches("^-?\\d+$")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput(promptMessage);
        }

        return Integer.parseInt(input);
    }

    public static Integer getInt(String promptMessage) {
        String input = getUserInput(promptMessage);

        while (!input.matches("^-?\\d+$")) {
            IO.printErrorMessage("Valore non valido");
            input = getUserInput(promptMessage);
        }

        return Integer.parseInt(input);
    }

    public static Location getLocationInput(boolean skippable) throws AbortOperationException {
        Location location = null;
        System.out.println("Puoi inserire l'indirizzo seguendo il formato indicato:");
        System.out.println("Esempio: Via Ottorino Rossi 9, Bizzozero, Italia");
        if (skippable) IO.printErrorMessage("\nDigitare '::skip' per saltare questo passaggio.\n");

        while (location == null) {
            try {
                String addrees = IO.getUserInput("Inserisci l'indirizzo nel seguente formato [indirizzo, citta, nazione]: ").trim();
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

    public static UUID parseUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static String replaceText(int colorCode, String text) {
        return "\u001B[" + colorCode + "m" + text + "\u001B[0m";
    }

    private static String printRow(int qta) {
        return "-".repeat(Math.max(0, (qta * 4) + 1));
    }

    public static void printErrorMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(31, "! ") + msg);
    }

    public static void printSuccessMessage(String msg) {
        if (msg == null) return;
        System.out.print("\n" + IO.replaceText(32, " ⩥ ") + msg);
    }

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
