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

    Optional<T> findById(UUID id);

    List<T> findAll();

    boolean save(T entity);

    boolean update(T entity);

    boolean delete(UUID id);
}
