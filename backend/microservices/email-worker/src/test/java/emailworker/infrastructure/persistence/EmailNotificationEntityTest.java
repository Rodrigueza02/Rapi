package emailworker.infrastructure.persistence;

import emailworker.domain.EmailNotificationType;
import emailworker.domain.EmailStatus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationEntityTest {

    @Test
    void create_setsInitialStatusAsPending() {
        EmailNotificationEntity e = build();
        assertThat(e.getStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(e.getCommandId()).isEqualTo("cmd-001");
        assertThat(e.getRecipientEmail()).isEqualTo("to@test.com");
    }

    @Test
    void markAccepted_setsStatusAndSentAt() {
        EmailNotificationEntity e = build();
        e.markAccepted();
        assertThat(e.getStatus()).isEqualTo(EmailStatus.SMTP_ACCEPTED);
        assertThat(e.getSentAt()).isNotNull();
    }

    @Test
    void markFailed_setsStatusAndErrorMessage() {
        EmailNotificationEntity e = build();
        e.markFailed("550 mailbox not found");
        assertThat(e.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(e.getErrorMessage()).isEqualTo("550 mailbox not found");
    }

    @Test
    void markUncertain_setsStatusAndReason() {
        EmailNotificationEntity e = build();
        e.markUncertain("timeout al enviar");
        assertThat(e.getStatus()).isEqualTo(EmailStatus.UNCERTAIN);
        assertThat(e.getErrorMessage()).isEqualTo("timeout al enviar");
    }

    @Test
    void markAccepted_thenMarkFailed_allowsOverwrite() {
        EmailNotificationEntity e = build();
        e.markAccepted();
        e.markFailed("error posterior");
        assertThat(e.getStatus()).isEqualTo(EmailStatus.FAILED);
    }

    private EmailNotificationEntity build() {
        return EmailNotificationEntity.create(
                "cmd-001", "to@test.com", "from@test.com",
                "reply@test.com", "Asunto", "Cuerpo",
                EmailNotificationType.ORDER_ASSIGNED, "order-42");
    }
}
