import it.uninsubria.laboratoriob.utils.Loader;

public class LoadTest {
    public static void main(String[] args) {

        long timestamp = System.currentTimeMillis();

        Loader.initialiseMaps();

        System.out.println(System.currentTimeMillis() - timestamp);
    }
}
