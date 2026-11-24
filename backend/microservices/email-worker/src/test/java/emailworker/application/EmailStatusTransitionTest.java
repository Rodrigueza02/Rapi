package emailworker.application;

import emailworker.domain.EmailStatus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailStatusTransitionTest {

    @Test
    void pending_canTransitionToSending() {
        assertThat(EmailStatus.PENDING.canTransitionTo(EmailStatus.SENDING)).isTrue();
    }

    @Test
    void pending_cannotTransitionToAccepted() {
        assertThat(EmailStatus.PENDING.canTransitionTo(EmailStatus.SMTP_ACCEPTED)).isFalse();
    }

    @Test
    void sending_canTransitionToAccepted() {
        assertThat(EmailStatus.SENDING.canTransitionTo(EmailStatus.SMTP_ACCEPTED)).isTrue();
    }

    @Test
    void sending_canTransitionToFailed() {
        assertThat(EmailStatus.SENDING.canTransitionTo(EmailStatus.FAILED)).isTrue();
    }

    @Test
    void sending_canTransitionToUncertain() {
        assertThat(EmailStatus.SENDING.canTransitionTo(EmailStatus.UNCERTAIN)).isTrue();
    }

    @Test
    void failed_canRetransitionToSending() {
        assertThat(EmailStatus.FAILED.canTransitionTo(EmailStatus.SENDING)).isTrue();
    }

    @Test
    void smtpAccepted_isTerminal() {
        for (EmailStatus next : EmailStatus.values()) {
            assertThat(EmailStatus.SMTP_ACCEPTED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    void uncertain_isTerminal() {
        for (EmailStatus next : EmailStatus.values()) {
            assertThat(EmailStatus.UNCERTAIN.canTransitionTo(next)).isFalse();
        }
    }
}
