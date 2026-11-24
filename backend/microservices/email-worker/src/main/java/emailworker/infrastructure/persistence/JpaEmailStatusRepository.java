package emailworker.infrastructure.persistence;

import emailworker.domain.EmailStatus;
import emailworker.port.EmailStatusRepositoryPort;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional
public class JpaEmailStatusRepository implements EmailStatusRepositoryPort {

    private final EmailNotificationJpaRepository jpa;

    public JpaEmailStatusRepository(EmailNotificationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailStatus> findStatusByCommandId(String commandId) {
        return jpa.findByCommandId(commandId)
                  .map(EmailNotificationEntity::getStatus);
    }

    @Override
    public boolean transitionStatus(String commandId, EmailStatus from, EmailStatus to) {
        return jpa.conditionalUpdateStatus(commandId, from, to, Instant.now()) == 1;
    }

    @Override
    public void markUncertain(String commandId, String reason) {
        jpa.findByCommandId(commandId).ifPresent(e -> e.markUncertain(reason));
    }

    @Override
    public void markSentAt(String commandId) {
        jpa.findByCommandId(commandId).ifPresent(EmailNotificationEntity::markAccepted);
    }

    @Override
    public void recordError(String commandId, String errorMessage) {
        jpa.findByCommandId(commandId).ifPresent(e -> e.markFailed(errorMessage));
    }

    @Override
    public int recoverStuckSending(Instant stuckBefore, String reason) {
        return jpa.markStuckSendingAsUncertain(
                stuckBefore, reason, Instant.now(),
                EmailStatus.SENDING, EmailStatus.UNCERTAIN);
    }
}
