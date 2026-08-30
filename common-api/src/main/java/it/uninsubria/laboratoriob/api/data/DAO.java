package it.uninsubria.laboratoriob.api.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contratto generico per l'accesso persistente a un'entità.
 * <p>
 * Le implementazioni concrete contengono le query reali.
 *
 * @param <T> tipo di entità gestita
 */
public interface DAO<T> {

    /**
     * Cerca un'entità per il suo identificativo.
     *
     * @param id UUID dell'entità
     * @return un {@link Optional} contenente l'entità trovata, oppure vuoto
     */
    Optional<T> findById(UUID id);

    /**
     * Restituisce tutte le entità gestite.
     *
     * @return lista di tutte le entità
     */
    List<T> findAll();

    /**
     * Restituisce una lista paginata di entità.
     *
     * @param offset indice di partenza (0-based)
     * @param limit  numero massimo di elementi
     * @return lista nella finestra richiesta
     */
    List<T> findAll(int offset, int limit);

    /**
     * Restituisce il numero totale di entità gestite.
     *
     * @return conteggio totale
     */
    long count();

    /**
     * Salva una nuova entità.
     *
     * @param entity entità da salvare
     * @return {@code true} se l'operazione ha successo
     */
    boolean save(T entity);

    /**
     * Aggiorna un'entità esistente.
     *
     * @param entity entità con i dati aggiornati
     * @return {@code true} se l'operazione ha successo
     */
    boolean update(T entity);

    /**
     * Elimina un'entità per il suo identificativo.
     *
     * @param id UUID dell'entità da eliminare
     * @return {@code true} se l'operazione ha successo
     */
    boolean delete(UUID id);
}
