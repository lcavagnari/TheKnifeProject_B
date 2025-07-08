import java.util.HashSet;
import java.util.Set;

public class UiTest {

    public static void main(String[] args) {
        Set<String> s = new HashSet<>();

        s.add("1");
        s.add("2");
        s.add("3");

        HashSet s1 = new HashSet(s);

        s.addAll(s1);
    }
}
