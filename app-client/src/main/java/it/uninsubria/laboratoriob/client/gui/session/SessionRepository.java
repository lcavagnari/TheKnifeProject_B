package it.uninsubria.laboratoriob.client.gui.session;

import it.uninsubria.laboratoriob.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

/**
 * Persiste l'id dell'utente loggato in un file .ini locale, per poter
 * ripristinare la sessione all'avvio della GUI.
 */
public class SessionRepository {

    private static final File SESSION_FILE = new File("data", "session.ini");
    private static final String USER_ID_KEY = "userId=";

    /**
     * Salva l'id dell'utente loggato nel file di sessione locale.
     *
     * @param user l'utente di cui persistere l'id
     */
    public void save(User user) {
        try {
            File dir = SESSION_FILE.getParentFile();
            if (dir != null && !dir.exists())
                dir.mkdirs();
            String content = "[session]\n" + USER_ID_KEY + user.getId() + "\n";
            Files.writeString(SESSION_FILE.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Errore salvando la sessione: " + e.getMessage());
        }
    }

    /**
     * Carica l'id dell'utente dalla sessione persistita nel file locale.
     *
     * @return un {@link Optional} contenente l'id dell'utente, oppure vuoto se la sessione non esiste
     */
    public Optional<UUID> loadUserId() {
        if (!SESSION_FILE.exists())
            return Optional.empty();
        try {
            for (String line : Files.readAllLines(SESSION_FILE.toPath(), StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.startsWith(USER_ID_KEY))
                    return Optional.of(UUID.fromString(trimmed.substring(USER_ID_KEY.length()).trim()));
            }
            return Optional.empty();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Errore leggendo la sessione: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Rimuove il file di sessione locale, terminando la sessione salvata.
     */
    public void clear() {
        if (SESSION_FILE.exists())
            SESSION_FILE.delete();
    }
}
