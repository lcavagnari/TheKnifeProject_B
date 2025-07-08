package it.uninsubria.laboratorioa.ui.exceptions;

import lombok.Getter;

public class AbortOperationException extends RuntimeException {

    @Getter
    final String reason;

    public AbortOperationException(String reason) {
        super("Operazione annullata");

        this.reason = reason;
    }

    public AbortOperationException() {
        super("Operazione annullata");
        this.reason = "";
    }
}
