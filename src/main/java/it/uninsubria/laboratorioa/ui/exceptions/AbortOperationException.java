package it.uninsubria.laboratorioa.ui.exceptions;

import lombok.Getter;


public class AbortOperationException extends RuntimeException {

    /**
     * Comando speciale che l'utente può inserire per annullare l'operazione corrente.<p>
     * Quando questo comando viene rilevato, viene lanciata un'eccezione {@link AbortOperationException}.
     */
    @Getter
    private static final String CANCEL_COMMAND = "::annulla";

    /**
     * Motivo dell'annullamento dell'operazione.<p>
     * Può essere una stringa vuota se non specificato.
     */
    @Getter
    private final String reason;

    /**
     * Costruttore con motivo dell'annullamento.<p>
     * Crea un'eccezione con messaggio "Operazione annullata" e un motivo specifico.
     *
     * @param reason il motivo per cui l'operazione è stata annullata
     */
    public AbortOperationException(String reason) {
        super("Operazione annullata");

        this.reason = reason;
    }

    /**
     * Costruttore di default senza motivo.<p>
     * Crea un'eccezione con messaggio "Operazione annullata" e motivo vuoto.
     */
    public AbortOperationException() {
        super("Operazione annullata");
        this.reason = "";
    }
}
