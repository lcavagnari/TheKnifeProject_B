package it.uninsubria.laboratoriob.api.exceptions;

/**
 * Segnala che un servizio remoto (RMI) non e' raggiungibile.<p>
 * Lanciata dai DAO client quando una {@link java.rmi.RemoteException} viene intercettata,
 * per permettere alla UI di mostrare un messaggio dedicato ("server non disponibile")
 * invece di fallire silenziosamente.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
