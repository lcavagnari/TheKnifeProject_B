package it.uninsubria.laboratoriob.client;

/**
 * Punto di ingresso del jar eseguibile.
 * <p>
 * Non estende {@link javafx.application.Application}: il launcher {@code java -jar}
 * verifica la gerarchia della classe indicata come {@code Main-Class} prima ancora
 * di invocarne il {@code main}, e richiede il module-path se questa estende
 * {@code Application} direttamente. Delegando a {@link TheKnifeClient#main} da qui,
 * il jar shaded resta avviabile anche con JavaFX solo sul classpath.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        TheKnifeClient.main(args);
    }
}
