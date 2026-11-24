package emailworker.domain;

public enum EmailStatus {

    PENDING,
    SENDING,
    SMTP_ACCEPTED,
    FAILED,
    UNCERTAIN;

    public boolean canTransitionTo(EmailStatus next) {
        return switch (this) {
            case PENDING       -> next == SENDING;
            case SENDING       -> next == SMTP_ACCEPTED || next == FAILED || next == UNCERTAIN;
            case FAILED        -> next == SENDING;      // reintento manual
            case SMTP_ACCEPTED -> false;                // terminal
            case UNCERTAIN     -> false;                // no auto-reintento
        };
    }
}
