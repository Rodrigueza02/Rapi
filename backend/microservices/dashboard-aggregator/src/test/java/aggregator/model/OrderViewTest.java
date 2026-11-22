
package aggregator.model;

import aggregator.state.OrderViewState;

import dtos.DriverAssignedEvent;
import dtos.DriverArrivingEvent;
import dtos.OrderDeliveredEvent;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderViewTest {

    private final Instant start =
            Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void shouldCreateAssignedOrder() {
        DriverAssignedEvent event =
                new DriverAssignedEvent(
                        "order-1", "driver-1", "customer-1", start);

        OrderView view = OrderView.from(event);

        assertEquals("order-1", view.getOrderId());
        assertEquals("driver-1", view.getDriverId());
        assertEquals(OrderViewState.ASSIGNED, view.getState());
    }

    @Test
    void shouldTransitionFromAssignedToArriving() {
        OrderView view = createOrder();

        boolean changed = view.apply(
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60))
        );

        assertTrue(changed);
        assertEquals(OrderViewState.ARRIVING, view.getState());
    }

    @Test
    void shouldTransitionFromArrivingToDelivered() {
        OrderView view = createOrder();

        view.apply(new DriverArrivingEvent(
                "order-1",
                "driver-1", "customer-1",
                start.plusSeconds(60)));

        view.apply(new OrderDeliveredEvent(
                "order-1",
                "driver-1", "customer-1",
                start.plusSeconds(120)));

        assertEquals(OrderViewState.DELIVERED, view.getState());
    }

    @Test
    void shouldRejectSkippingArrivingState() {
        OrderView view = createOrder();

        assertThrows(
                IllegalStateException.class,
                () -> view.apply(
                        new OrderDeliveredEvent(
                                "order-1",
                                "driver-1", "customer-1",
                                start.plusSeconds(120)))
        );
    }

    @Test
    void shouldIgnoreExactDuplicateEvent() {
        OrderView view = createOrder();

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60));

        assertTrue(view.apply(event));
        assertFalse(view.apply(event));
    }

    @Test
    void shouldRejectEventFromAnotherOrder() {
        OrderView view = createOrder();

        assertThrows(
                IllegalArgumentException.class,
                () -> view.apply(
                        new DriverArrivingEvent(
                                "order-2",
                                "driver-1", "customer-1",
                                start.plusSeconds(60)))
        );
    }

    private OrderView createOrder() {
        return OrderView.from(
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start)
        );
    }
}
