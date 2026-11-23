package emailworker.port;

import emailworker.domain.EmailStatus;

import java.time.Instant;
import java.util.Optional;

public interface EmailStatusRepositoryPort {

    Optional<EmailStatus> findStatusByCommandId(String commandId);

    boolean transitionStatus(String commandId, EmailStatus from, EmailStatus to);

    void markUncertain(String commandId, String reason);

    void markSentAt(String commandId);

    void recordError(String commandId, String errorMessage);

    int recoverStuckSending(Instant stuckBefore, String reason);
}
