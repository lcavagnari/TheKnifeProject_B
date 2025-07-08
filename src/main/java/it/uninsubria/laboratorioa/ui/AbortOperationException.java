package it.uninsubria.laboratorioa.ui;

public class AbortOperationException extends RuntimeException {

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
