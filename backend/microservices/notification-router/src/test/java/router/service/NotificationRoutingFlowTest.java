package notificationrouter.service;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.NotificationCommand;
import dtos.OrderDeliveredEvent;
import notificationrouter.strategy.ClientSmsStrategy;
import notificationrouter.strategy.DriverPushStrategy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** El servicio con las estrategias reales; el puerto de salida es una simple lista. */
class NotificationRoutingFlowTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    private final List<NotificationCommand> published = new ArrayList<>();

    private final NotificationRouterService service = new NotificationRouterService(
            List.of(new ClientSmsStrategy(), new DriverPushStrategy()), published::add);

    @Test
    void arrivingEventSendsOneSmsToTheCustomer() {
        service.route(new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW));

        assertThat(published).hasSize(1);
        assertThat(published.get(0).routingKey()).isEqualTo("notify.client.sms");
        assertThat(published.get(0).payload().recipientId()).isEqualTo("customer-1");
    }

    @Test
    void deliveredEventSendsOnePushToTheDriver() {
        service.route(new OrderDeliveredEvent("order-1", "driver-1", "customer-1", NOW));

        assertThat(published).hasSize(1);
        assertThat(published.get(0).routingKey()).isEqualTo("notify.driver.push");
        assertThat(published.get(0).payload().recipientId()).isEqualTo("driver-1");
    }

    @Test
    void assignedEventSendsOnePushToTheDriver() {
        service.route(new DriverAssignedEvent("order-1", "driver-1", "customer-1", NOW));

        assertThat(published).hasSize(1);
        assertThat(published.get(0).routingKey()).isEqualTo("notify.driver.push");
    }
}