package dtos;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationCommandTest {

    private final NotificationPayloadRecord payload =
            new NotificationPayloadRecord("order-1", "customer-1", "Your driver is arriving");

    @Test
    void createsCommandWithGeneratedId() {
        // Act
        NotificationCommand command = NotificationCommand.of(Names.ROUTING_CLIENT_SMS, payload);

        // Assert
        assertThat(command.commandId()).isNotBlank();
        assertThat(command.routingKey()).isEqualTo("notify.client.sms");
    }

    @Test
    void rejectsUnknownRoutingKey() {
        assertThatThrownBy(() -> NotificationCommand.of("notify.pigeon.mail", payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("routing_key");
    }

    @Test
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> new NotificationPayloadRecord("order-1", "customer-1", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");
    }
}