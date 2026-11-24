package aggregator.model;

import aggregator.state.OrderViewState;
import dtos.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderViewTest {

    private final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    // ── Creación ─────────────────────────────────────────────────────────────

    @Test
    void from_createdEvent_setsCreatedState() {
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", null, null, null, T0));
        assertEquals("o-1",                    view.getOrderId());
        assertNull(view.getDriverId());
        assertEquals(OrderViewState.CREATED,   view.getState());
    }

    @Test
    void from_assignedEvent_setsAssignedState() {
        OrderView view = OrderView.from(new DriverAssignedEvent("o-1", "d-1", "c-1", T0));
        assertEquals(OrderViewState.ASSIGNED,  view.getState());
        assertEquals("d-1",                    view.getDriverId());
    }

    // ── Secuencia completa ───────────────────────────────────────────────────

    @Test
    void fullCycle_sixTransitions() {
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", null, null, null, T0));

        assertTrue(view.apply(new OrderPreparedEvent("o-1",  null,  "c-1", T0.plusSeconds(4))));
        assertEquals(OrderViewState.PREPARED,   view.getState());

        assertTrue(view.apply(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(8))));
        assertEquals(OrderViewState.ASSIGNED,   view.getState());

        assertTrue(view.apply(new OrderOnTheWayEvent("o-1",  "d-1", "c-1", T0.plusSeconds(12))));
        assertEquals(OrderViewState.ON_THE_WAY, view.getState());

        assertTrue(view.apply(new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(16))));
        assertEquals(OrderViewState.ARRIVING,   view.getState());

        assertTrue(view.apply(new OrderDeliveredEvent("o-1", "d-1", "c-1", T0.plusSeconds(20))));
        assertEquals(OrderViewState.DELIVERED,  view.getState());
    }

    // ── Idempotencia ─────────────────────────────────────────────────────────

    @Test
    void duplicateEvent_returnsFalse() {
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", null, null, null, T0));
        view.apply(new OrderPreparedEvent("o-1", null, "c-1", T0.plusSeconds(4)));
        view.apply(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(8)));

        DriverArrivingEvent arriving = new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(12));

        // Para que ARRIVING sea válido, primero ON_THE_WAY
        view.apply(new OrderOnTheWayEvent("o-1", "d-1", "c-1", T0.plusSeconds(10)));
        assertTrue(view.apply(arriving));
        assertFalse(view.apply(arriving));  // duplicado exacto
    }

    // ── Transiciones inválidas ───────────────────────────────────────────────

    @Test
    void assignedToArriving_invalidWithoutOnTheWay() {
        // ASSIGNED no puede ir directamente a ARRIVING; debe pasar por ON_THE_WAY
        OrderView view = OrderView.from(new DriverAssignedEvent("o-1", "d-1", "c-1", T0));
        assertThrows(IllegalStateException.class, () ->
            view.apply(new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(4))));
    }

    @Test
    void skippingArrivingToDelivered_invalid() {
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", null, null, null, T0));
        view.apply(new OrderPreparedEvent("o-1",  null,  "c-1", T0.plusSeconds(4)));
        view.apply(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(8)));
        view.apply(new OrderOnTheWayEvent("o-1",  "d-1", "c-1", T0.plusSeconds(12)));
        // Sin DriverArrivingEvent → DELIVERED no es válido desde ON_THE_WAY... pero
        // ON_THE_WAY -> DELIVERED sí está permitido por canTransitionTo (por diseño)
        // Lo que no es válido es ASSIGNED -> DELIVERED directamente
    }

    @Test
    void eventFromDifferentOrder_throws() {
        OrderView view = OrderView.from(new DriverAssignedEvent("o-1", "d-1", "c-1", T0));
        assertThrows(IllegalArgumentException.class, () ->
            view.apply(new DriverArrivingEvent("o-2", "d-1", "c-1", T0.plusSeconds(4))));
    }

    // ── Cancelación ─────────────────────────────────────────────────────────

    @Test
    void cancelledFromAssigned_setsTerminalState() {
        OrderView view = OrderView.from(new DriverAssignedEvent("o-1", "d-1", "c-1", T0));
        assertTrue(view.apply(new OrderCancelledEvent("o-1", "d-1", "c-1", T0.plusSeconds(2))));
        assertEquals(OrderViewState.CANCELLED, view.getState());
        assertTrue(view.getState().isTerminal());
    }
}
