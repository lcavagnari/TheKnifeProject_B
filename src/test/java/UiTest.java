import it.uninsubria.laboratorioa.ui.Menus;
import it.uninsubria.laboratorioa.utils.Loader;

public class UiTest {

    public static void main(String[] args) {
        System.out.println("Hello, World!");
        Loader.loadFromFile();
        Menus.mainMenu();
    }
}
