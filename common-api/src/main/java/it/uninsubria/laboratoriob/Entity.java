package it.uninsubria.laboratoriob;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Classe astratta base per tutte le entità persistite su database.
 * <p>
 * Gestisce l'identificatore univoco UUID condiviso da tutte le entità.
 * <p>
 * La persistenza (salvataggio, lettura, aggiornamento) è delegata ai it.uninsubria.laboratoriob.DAO
 * in {@code it.uninsubria.laboratorioa.db}; questa classe non conosce
 * il layer di storage.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 2.0
 */
public abstract class Entity {

    /**
     * Identificatore univoco dell'entità.
     */
    @Getter
    protected final UUID id;

    /**
     * Costruttore che inizializza l'entità con un UUID esistente
     * (tipicamente usato per ricostruire un'entità già persistita).
     * <p>
     *
     * @param id UUID esistente
     */
    public Entity(UUID id) {
        Validators.validateUUID(id);
        this.id = id;
    }

    /**
     * Costruttore che crea una nuova entità con UUID casuale.
     * <p>
     */
    public Entity() {
        this.id = UUID.randomUUID();
    }

    /**
     * Restituisce una stringa base rappresentante l'entità.
     * <p>
     *
     * @return stringa contenente l'id
     */
    @Override
    public String toString() {
        return "id=" + id;
    }

    /**
     * Controlla l'uguaglianza con un altro oggetto basandosi sull'UUID e la classe.
     * <p>
     *
     * @param o oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Entity that = (Entity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcola l'hash code basandosi sull'UUID.
     * <p>
     *
     * @return hash code calcolato
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
