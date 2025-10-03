package it.uninsubria.laboratorioa.ui.exceptions;

import lombok.Getter;


public class AbortOperationException extends RuntimeException {

    @Getter
    private static final String CANCEL_COMMAND = "::annulla";

    @Getter
    private final String reason;

    public AbortOperationException(String reason) {
        super("Operazione annullata");

        this.reason = reason;
    }

    public AbortOperationException() {
        super("Operazione annullata");
        this.reason = "";
    }
}
