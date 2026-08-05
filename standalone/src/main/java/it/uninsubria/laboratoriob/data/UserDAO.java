package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.DAO;
import it.uninsubria.laboratoriob.objects.User;

import java.util.Optional;

/**
 * Contratto it.uninsubria.laboratoriob.DAO comune ai sottotipi di {@link User} ({@code Client},
 * {@code Owner}).
 * <p>
 *
 * @param <T> sottotipo concreto di User
 */
public interface UserDAO<T extends User> extends DAO<T> {

    /**
     * Recupera un utente per username.
     * <p>
     *
     * @param username nome utente
     * @return utente corrispondente, se presente
     */
    Optional<T> findByUsername(String username);
}
