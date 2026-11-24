package emailworker.infrastructure.persistence;

import emailworker.domain.EmailNotificationType;
import emailworker.domain.EmailStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaEmailStatusRepositoryTest {

    @Mock EmailNotificationJpaRepository jpa;
    @InjectMocks JpaEmailStatusRepository repo;

    EmailNotificationEntity entity;

    @BeforeEach
    void setUp() {
        entity = EmailNotificationEntity.create("cmd-001", "to@test.com", "from@test.com",
                "reply@test.com", "Asunto", "Cuerpo", EmailNotificationType.ADMIN_MANUAL, null);
    }

    @Test
    void findStatusByCommandId_returnsPresentWhenFound() {
        when(jpa.findByCommandId("cmd-001")).thenReturn(Optional.of(entity));
        assertThat(repo.findStatusByCommandId("cmd-001")).contains(EmailStatus.PENDING);
    }

    @Test
    void findStatusByCommandId_returnsEmptyWhenNotFound() {
        when(jpa.findByCommandId("cmd-999")).thenReturn(Optional.empty());
        assertThat(repo.findStatusByCommandId("cmd-999")).isEmpty();
    }

    @Test
    void transitionStatus_returnsTrueWhenRowUpdated() {
        when(jpa.conditionalUpdateStatus(eq("cmd-001"),
                eq(EmailStatus.PENDING), eq(EmailStatus.SENDING), any())).thenReturn(1);
        assertThat(repo.transitionStatus("cmd-001", EmailStatus.PENDING, EmailStatus.SENDING)).isTrue();
    }

    @Test
    void transitionStatus_returnsFalseWhenNoRowUpdated() {
        when(jpa.conditionalUpdateStatus(eq("cmd-001"),
                eq(EmailStatus.PENDING), eq(EmailStatus.SENDING), any())).thenReturn(0);
        assertThat(repo.transitionStatus("cmd-001", EmailStatus.PENDING, EmailStatus.SENDING)).isFalse();
    }

    @Test
    void markUncertain_updatesEntityStatus() {
        when(jpa.findByCommandId("cmd-001")).thenReturn(Optional.of(entity));
        repo.markUncertain("cmd-001", "timeout SMTP");
        assertThat(entity.getStatus()).isEqualTo(EmailStatus.UNCERTAIN);
        assertThat(entity.getErrorMessage()).isEqualTo("timeout SMTP");
    }

    @Test
    void markSentAt_marksEntityAsAccepted() {
        when(jpa.findByCommandId("cmd-001")).thenReturn(Optional.of(entity));
        repo.markSentAt("cmd-001");
        assertThat(entity.getStatus()).isEqualTo(EmailStatus.SMTP_ACCEPTED);
        assertThat(entity.getSentAt()).isNotNull();
    }

    @Test
    void recordError_marksEntityAsFailed() {
        when(jpa.findByCommandId("cmd-001")).thenReturn(Optional.of(entity));
        repo.recordError("cmd-001", "mailbox not found");
        assertThat(entity.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(entity.getErrorMessage()).isEqualTo("mailbox not found");
    }

    @Test
    void recoverStuckSending_delegatesToJpa() {
        Instant threshold = Instant.now().minusSeconds(300);
        when(jpa.markStuckSendingAsUncertain(eq(threshold), eq("stuck"), any(),
                eq(EmailStatus.SENDING), eq(EmailStatus.UNCERTAIN))).thenReturn(3);
        assertThat(repo.recoverStuckSending(threshold, "stuck")).isEqualTo(3);
    }
}
