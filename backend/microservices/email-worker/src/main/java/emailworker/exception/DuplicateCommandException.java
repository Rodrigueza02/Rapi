package emailworker.exception;

import emailworker.domain.EmailStatus;

public class DuplicateCommandException extends RuntimeException {

    private final String commandId;
    private final EmailStatus currentStatus;

    public DuplicateCommandException(String commandId, EmailStatus currentStatus) {
        super("Comando ya procesado o en estado no reprocesable: commandId="
                + commandId + ", status=" + currentStatus);
        this.commandId = commandId;
        this.currentStatus = currentStatus;
    }

    public String getCommandId() {
        return commandId;
    }

    public EmailStatus getCurrentStatus() {
        return currentStatus;
    }
}
