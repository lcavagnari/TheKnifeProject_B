package it.uninsubria.laboratoriob.client.gui.session;

import it.uninsubria.laboratoriob.api.objects.User;

/**
 * Tiene traccia dell'utente correntemente loggato nella GUI.
 * Semplice singleton in-memory: quando ci sarà il server, il login
 * passerà da una vera sessione/token, ma i controller possono già
 * appoggiarsi a questa classe per non doverla riscrivere.
 */
public final class Session {

    private static User currentUser;

    private Session() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
