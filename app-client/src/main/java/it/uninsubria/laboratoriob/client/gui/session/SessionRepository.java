package it.uninsubria.laboratoriob.client.gui.session;

import it.uninsubria.laboratoriob.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Persiste solo lo username dell'utente loggato in un file locale.
 */
public class SessionRepository {

    private static final File SESSION_FILE = new File("data", "session.txt");

    public void save(User user) {
        try {
            File dir = SESSION_FILE.getParentFile();
            if (dir != null && !dir.exists())
                dir.mkdirs();
            Files.writeString(SESSION_FILE.toPath(), user.getUsername(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Errore salvando la sessione: " + e.getMessage());
        }
    }

    public Optional<String> loadUsername() {
        if (!SESSION_FILE.exists())
            return Optional.empty();
        try {
            String username = Files.readString(SESSION_FILE.toPath(), StandardCharsets.UTF_8).trim();
            return username.isBlank() ? Optional.empty() : Optional.of(username);
        } catch (IOException e) {
            System.err.println("Errore leggendo la sessione: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void clear() {
        if (SESSION_FILE.exists())
            SESSION_FILE.delete();
    }
}
