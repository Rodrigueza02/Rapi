
package pushworker.service;

import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushSenderServiceTest {

    private PushSenderService pushSenderService;

    @BeforeEach
    void setUp() {
        pushSenderService = new PushSenderService();
    }

    @Test
    void shouldSendPushWhenRoutingKeyIsValid() {

        // Arrange
        NotificationCommand command =
                mock(NotificationCommand.class);

        NotificationPayloadRecord payload =
                mock(NotificationPayloadRecord.class);

        when(command.routingKey())
                .thenReturn(Names.ROUTING_DRIVER_PUSH);

        when(command.payload())
                .thenReturn(payload);

        when(payload.orderId())
                .thenReturn("ORDER-001");

        when(payload.recipientId())
                .thenReturn("DRIVER-001");

        when(payload.message())
                .thenReturn("Tienes un nuevo pedido");

        // Act & Assert
        assertDoesNotThrow(
                () -> pushSenderService.send(command)
        );

        verify(command).routingKey();
        verify(command).payload();

        verify(payload).orderId();
        verify(payload).recipientId();
        verify(payload).message();
    }

    @Test
    void shouldThrowExceptionWhenRoutingKeyIsInvalid() {

        // Arrange
        NotificationCommand command =
                mock(NotificationCommand.class);

        when(command.routingKey())
                .thenReturn("invalid.routing.key");

        // Act
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pushSenderService.send(command)
                );

        // Assert
        assertTrue(
                exception.getMessage()
                        .contains("Unsupported push routing key")
        );

        verify(command, never()).payload();
    }

    @Test
    void shouldThrowExceptionWhenRoutingKeyIsNull() {

        // Arrange
        NotificationCommand command =
                mock(NotificationCommand.class);

        when(command.routingKey())
                .thenReturn(null);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> pushSenderService.send(command)
        );

        verify(command, never()).payload();
    }
}
