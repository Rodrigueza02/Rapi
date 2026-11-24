package emailworker.exception;

public class EmailSendException extends RuntimeException {

    private final String commandId;
    private final boolean permanent;

    /**
     * @param commandId identificador único de la solicitud
     * @param message   descripción del error
     * @param cause     excepción original del proveedor SMTP
     * @param permanent true = error definitivo (no reintentable);
     *                  false = error transitorio (podría funcionar luego)
     */
    public EmailSendException(String commandId,
                               String message,
                               Throwable cause,
                               boolean permanent) {
        super(message, cause);
        this.commandId = commandId;
        this.permanent = permanent;
    }

    public String getCommandId() {
        return commandId;
    }

    /**
     * Indica si el error es definitivo (FAILED) o transitorio.
     * Un error no permanente producirá estado UNCERTAIN en lugar de FAILED.
     */
    public boolean isPermanent() {
        return permanent;
    }
}
