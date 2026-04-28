import it.uninsubria.laboratorioa.utils.Loader;

public class LoadTest {
    public static void main(String[] args) {

        long timestamp = System.currentTimeMillis();

        Loader.loadFromFile();

        System.out.println(System.currentTimeMillis() - timestamp);
    }
}
