package notificationrouter.strategy;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.Names;
import dtos.NotificationCommand;
import dtos.OrderDeliveredEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DriverPushStrategyTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    private final DriverPushStrategy strategy = new DriverPushStrategy();

    @Test
    void supportsAssignedAndDeliveredButNotArriving() {
        assertThat(strategy.supports(new DriverAssignedEvent("order-1", "driver-1", "customer-1", NOW))).isTrue();
        assertThat(strategy.supports(new OrderDeliveredEvent("order-1", "driver-1", "customer-1", NOW))).isTrue();
        assertThat(strategy.supports(new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW))).isFalse();
    }

    @Test
    void mapsTheAssignmentToAPushForTheDriver() {
        // Act
        NotificationCommand command = strategy
                .map(new DriverAssignedEvent("order-1", "driver-1", "customer-1", NOW))
                .orElseThrow();

        // Assert
        assertThat(command.routingKey()).isEqualTo(Names.ROUTING_DRIVER_PUSH);
        assertThat(command.payload().recipientId()).isEqualTo("driver-1");
        assertThat(command.payload().message()).isEqualTo("New delivery assigned");
    }

    @Test
    void mapsTheDeliveryToAPushForTheDriver() {
        // Act
        NotificationCommand command = strategy
                .map(new OrderDeliveredEvent("order-1", "driver-1", "customer-1", NOW))
                .orElseThrow();

        // Assert
        assertThat(command.routingKey()).isEqualTo(Names.ROUTING_DRIVER_PUSH);
        assertThat(command.payload().recipientId()).isEqualTo("driver-1");
        assertThat(command.payload().message()).isEqualTo("Delivery completed");
    }

    @Test
    void returnsNothingForTheArrivingEvent() {
        assertThat(strategy.map(new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW))).isEmpty();
    }
}