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

    /**
     * Imposta l'utente corrente come loggato.
     *
     * @param user l'utente autenticato
     */
    public static void login(User user) {
        currentUser = user;
    }

    /**
     * Termina la sessione corrente, rimuovendo l'utente loggato.
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Restituisce l'utente attualmente autenticato.
     *
     * @return l'utente corrente, oppure {@code null} se nessuno è loggato
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Verifica se c'è un utente attualmente autenticato.
     *
     * @return {@code true} se un utente è loggato, {@code false} altrimenti
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
