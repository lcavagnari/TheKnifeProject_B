package it.uninsubria.laboratoriob.api.enums;

/**
 * Enum che rappresenta i ruoli disponibili per gli utenti del sistema.
 * <p>
 * Ogni utente autenticato assume uno dei ruoli definiti in questo enum,
 * che determina le operazioni disponibili nell'interfaccia a linea di comando.
 * </p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 * @see it.uninsubria.laboratoriob.api.objects.User#getRole()
 */
public enum UserRole {
    /**
     * Ruolo cliente: può cercare ristoranti, scrivere recensioni e gestire preferiti.
     */
    CLIENT,
    /**
     * Ruolo proprietario: può gestire i propri ristoranti e rispondere alle recensioni.
     */
    OWNER
}
