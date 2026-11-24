package notificationrouter.strategy;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.Names;
import dtos.NotificationCommand;
import dtos.OrderDeliveredEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ClientSmsStrategyTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    private final ClientSmsStrategy strategy = new ClientSmsStrategy();

    @Test
    void supportsOnlyTheArrivingEvent() {
        assertThat(strategy.supports(new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW))).isTrue();
        assertThat(strategy.supports(new DriverAssignedEvent("order-1", "driver-1", "customer-1", NOW))).isFalse();
        assertThat(strategy.supports(new OrderDeliveredEvent("order-1", "driver-1", "customer-1", NOW))).isFalse();
    }

    @Test
    void mapsTheArrivalToAnSmsForTheCustomer() {
        // Arrange
        DriverArrivingEvent event = new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW);

        // Act
        NotificationCommand command = strategy.map(event).orElseThrow();

        // Assert
        assertThat(command.routingKey()).isEqualTo(Names.ROUTING_CLIENT_SMS);
        assertThat(command.payload().orderId()).isEqualTo("order-1");
        assertThat(command.payload().recipientId()).isEqualTo("customer-1");
        assertThat(command.payload().message()).isEqualTo("Your driver is arriving");
    }

    @Test
    void returnsNothingForOtherEvents() {
        assertThat(strategy.map(new OrderDeliveredEvent("order-1", "driver-1", "customer-1", NOW))).isEmpty();
    }
}