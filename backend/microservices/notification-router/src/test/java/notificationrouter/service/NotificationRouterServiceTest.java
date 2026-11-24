package notificationrouter.service;

import dtos.DriverArrivingEvent;
import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;
import notificationrouter.ports.NotificationCommandPort;
import notificationrouter.strategy.NotificationMappingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Prueba la ORQUESTACIÓN del servicio con estrategias falsas (Mockito).
 * Qué dice cada estrategia real se prueba en las pruebas de strategy/.
 */
@ExtendWith(MockitoExtension.class)
class NotificationRouterServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    @Mock
    private NotificationMappingStrategy firstStrategy;

    @Mock
    private NotificationMappingStrategy secondStrategy;

    @Mock
    private NotificationCommandPort notificationCommandPort;

    private NotificationRouterService service;

    private final DriverArrivingEvent event =
            new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW);

    private final NotificationCommand command = NotificationCommand.of(
            Names.ROUTING_CLIENT_SMS,
            new NotificationPayloadRecord("order-1", "customer-1", "Your driver is arriving"));

    @BeforeEach
    void setUp() {
        service = new NotificationRouterService(
                List.of(firstStrategy, secondStrategy), notificationCommandPort);
    }

    @Test
    void publishesTheCommandOfTheFirstStrategyThatSupportsTheEvent() {
        // Arrange
        when(firstStrategy.supports(event)).thenReturn(true);
        when(firstStrategy.map(event)).thenReturn(Optional.of(command));

        // Act
        service.route(event);

        // Assert
        verify(notificationCommandPort).publish(command);
        verifyNoInteractions(secondStrategy);
    }

    @Test
    void skipsStrategiesThatDoNotSupportTheEvent() {
        // Arrange
        when(firstStrategy.supports(event)).thenReturn(false);
        when(secondStrategy.supports(event)).thenReturn(true);
        when(secondStrategy.map(event)).thenReturn(Optional.of(command));

        // Act
        service.route(event);

        // Assert
        verify(firstStrategy, never()).map(any());
        verify(notificationCommandPort).publish(command);
    }

    @Test
    void publishesNothingWhenTheStrategyReturnsNoCommand() {
        // Arrange
        when(firstStrategy.supports(event)).thenReturn(true);
        when(firstStrategy.map(event)).thenReturn(Optional.empty());

        // Act
        service.route(event);

        // Assert
        verifyNoInteractions(notificationCommandPort);
    }

    @Test
    void silentlySkipsWhenNoStrategySupportsTheEvent() {
        // Arrange
        when(firstStrategy.supports(event)).thenReturn(false);
        when(secondStrategy.supports(event)).thenReturn(false);

        // Act — should NOT throw; unhandled events are logged at DEBUG level
        service.route(event);

        // Assert
        verifyNoInteractions(notificationCommandPort);
    }

    @Test
    void rejectsANullEvent() {
        assertThatThrownBy(() -> service.route(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }
}