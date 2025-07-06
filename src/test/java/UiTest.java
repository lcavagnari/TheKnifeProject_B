public class UiTest {
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


    public static void main(String[] args) {
        System.out.println("Hello, World!");

        clearScreen();
        System.out.println("Hello, Wod!");
    }
}
