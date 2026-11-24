package processor.model;

import org.junit.jupiter.api.Test;
import processor.exception.InvalidStateTransitionException;
import processor.state.OrderState;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private static final Instant T0 = Instant.parse("2026-11-21T10:00:00Z");

    private Order newOrder() {
        return Order.create("order-1", "customer-1", "Calle 18 # 25-10, Pasto",
                new BigDecimal("25000"), T0);
    }
    private DriverAssignment da(String id) { return new DriverAssignment(id, T0.plusSeconds(5)); }

    // ── Ciclo completo CREATED→PREPARED→ASSIGNED→ON_THE_WAY→ARRIVING→DELIVERED ──

    @Test void createsOrderInCreatedState() {
        assertThat(newOrder().statusName()).isEqualTo("CREATED");
    }

    @Test void completesFullLifecycle() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));  assertThat(o.statusName()).isEqualTo("PREPARED");
        o.assignDriver(da("d-1"));     assertThat(o.statusName()).isEqualTo("ASSIGNED");
        o.startDelivery();             assertThat(o.statusName()).isEqualTo("ON_THE_WAY");
        o.registerArrival(T0.plusSeconds(60));  assertThat(o.statusName()).isEqualTo("ON_THE_WAY");
        o.completeDelivery(T0.plusSeconds(90)); assertThat(o.statusName()).isEqualTo("DELIVERED");
        assertThat(o.deliveredAt()).isPresent();
    }

    // ── Cancelación ───────────────────────────────────────────────────────────

    @Test void cancelsFromCreated() {
        Order o = newOrder();
        o.cancel();
        assertThat(o.statusName()).isEqualTo("CANCELLED");
    }

    @Test void cancelsFromPrepared() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));
        o.cancel();
        assertThat(o.statusName()).isEqualTo("CANCELLED");
    }

    @Test void cancelsFromAssigned() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));
        o.assignDriver(da("d-1"));
        o.cancel();
        assertThat(o.statusName()).isEqualTo("CANCELLED");
    }

    @Test void rejectsCancelFromDelivered() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));
        o.assignDriver(da("d-1"));
        o.startDelivery();
        o.registerArrival(T0.plusSeconds(60));
        o.completeDelivery(T0.plusSeconds(90));
        assertThatThrownBy(o::cancel).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test void rejectsCancelFromOnTheWay() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));
        o.assignDriver(da("d-1"));
        o.startDelivery();
        assertThatThrownBy(o::cancel).isInstanceOf(InvalidStateTransitionException.class);
    }

    // ── Transiciones inválidas ────────────────────────────────────────────────

    @Test void rejectsDeliveryFromCreated() {
        assertThatThrownBy(() -> newOrder().completeDelivery(T0))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test void rejectsSkippingPrepare() {
        // CREATED → directamente startDelivery debe fallar
        assertThatThrownBy(() -> newOrder().startDelivery())
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test void rejectsDuplicateDriver() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));
        o.assignDriver(da("d-1"));
        assertThatThrownBy(() -> o.assignDriver(da("d-2")))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test void rejectsDuplicateArrival() {
        Order o = newOrder();
        o.prepare(T0.plusSeconds(1));
        o.assignDriver(da("d-1"));
        o.startDelivery();
        o.registerArrival(T0.plusSeconds(60));
        assertThatThrownBy(() -> o.registerArrival(T0.plusSeconds(70)))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    @Test void restoreInOnTheWay() {
        Order o = Order.restore("order-1", "c-1", "Calle 18", BigDecimal.TEN, T0,
                OrderState.fromName("ON_THE_WAY"), da("d-9"), null, null, null);
        o.completeDelivery(T0.plusSeconds(30));
        assertThat(o.statusName()).isEqualTo("DELIVERED");
    }

    @Test void restoreInPrepared() {
        Order o = Order.restore("order-1", "c-1", "Calle 18", BigDecimal.TEN, T0,
                OrderState.fromName("PREPARED"), null, T0.plusSeconds(2), null, null);
        assertThat(o.statusName()).isEqualTo("PREPARED");
        assertThat(o.preparedAt()).isPresent();
    }
}
