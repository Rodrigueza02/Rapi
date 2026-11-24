package aggregator.service;

import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;
import dtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardAggregatorServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-21T10:00:00Z");

    private OrderViewRepositoryPort    repo;
    private DashboardBroadcastPort     broadcaster;
    private DashboardAggregatorService service;

    @BeforeEach
    void setUp() {
        repo        = mock(OrderViewRepositoryPort.class);
        broadcaster = mock(DashboardBroadcastPort.class);
        service     = new DashboardAggregatorService(repo, broadcaster);
    }

    // ── Creación de vistas iniciales ─────────────────────────────────────────

    @Test
    void processCreatedEvent_createsNewView() {
        when(repo.findById("o-1")).thenReturn(Optional.empty());
        service.process(new OrderCreatedEvent("o-1", null, "c-1", T0));
        verify(repo).save(any(OrderView.class), eq(Names.EVENT_ORDER_CREATED));
        verify(broadcaster).broadcast(any(OrderView.class));
    }

    @Test
    void processPreparedEvent_createsNewViewWhenNoneExists() {
        when(repo.findById("o-1")).thenReturn(Optional.empty());
        service.process(new OrderPreparedEvent("o-1", null, "c-1", T0));
        verify(repo).save(any(OrderView.class), eq(Names.EVENT_ORDER_PREPARED));
        verify(broadcaster).broadcast(any(OrderView.class));
    }

    @Test
    void processDriverAssigned_updatesExistingView() {
        OrderView view = OrderView.from(new OrderPreparedEvent("o-1", null, "c-1", T0));
        when(repo.findById("o-1")).thenReturn(Optional.of(view));
        service.process(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(4)));
        verify(repo).save(eq(view), eq(Names.EVENT_DRIVER_ASSIGNED));
    }

    // ── Secuencia completa ───────────────────────────────────────────────────

    @Test
    void fullLifecycle_sixTransitions() {
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", T0));
        when(repo.findById("o-1")).thenReturn(Optional.of(view));

        service.process(new OrderPreparedEvent("o-1", null, "c-1", T0.plusSeconds(4)));
        assertThat(view.getState()).isEqualTo(OrderViewState.PREPARED);

        service.process(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(8)));
        assertThat(view.getState()).isEqualTo(OrderViewState.ASSIGNED);

        service.process(new OrderOnTheWayEvent("o-1", "d-1", "c-1", T0.plusSeconds(12)));
        assertThat(view.getState()).isEqualTo(OrderViewState.ON_THE_WAY);

        service.process(new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(16)));
        assertThat(view.getState()).isEqualTo(OrderViewState.ARRIVING);

        service.process(new OrderDeliveredEvent("o-1", "d-1", "c-1", T0.plusSeconds(20)));
        assertThat(view.getState()).isEqualTo(OrderViewState.DELIVERED);

        // 5 transiciones aplicadas (CREATED ya era el estado inicial)
        verify(repo, times(5)).save(eq(view), anyString());
    }

    // ── Stats ────────────────────────────────────────────────────────────────

    @Test
    void stats_activeExcludesDeliveredAndCancelled() {
        List<OrderView> all = new ArrayList<>();

        // CREATED → activo
        all.add(OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", T0)));

        // PREPARED → activo
        all.add(OrderView.from(new OrderPreparedEvent("o-2", null, "c-2", T0)));

        // ASSIGNED → activo
        OrderView assigned = OrderView.from(new OrderPreparedEvent("o-3", null, "c-3", T0));
        assigned.apply(new DriverAssignedEvent("o-3", "d-1", "c-3", T0.plusSeconds(4)));
        all.add(assigned);

        // DELIVERED → no activo
        OrderView delivered = OrderView.from(new OrderCreatedEvent("o-4", null, "c-4", T0));
        delivered.apply(new OrderPreparedEvent("o-4",  null,  "c-4", T0.plusSeconds(4)));
        delivered.apply(new DriverAssignedEvent("o-4", "d-1", "c-4", T0.plusSeconds(8)));
        delivered.apply(new OrderOnTheWayEvent("o-4",  "d-1", "c-4", T0.plusSeconds(12)));
        delivered.apply(new DriverArrivingEvent("o-4", "d-1", "c-4", T0.plusSeconds(16)));
        delivered.apply(new OrderDeliveredEvent("o-4", "d-1", "c-4", T0.plusSeconds(20)));
        all.add(delivered);

        // CANCELLED → no activo
        OrderView cancelled = OrderView.from(new OrderPreparedEvent("o-5", null, "c-5", T0));
        cancelled.apply(new OrderCancelledEvent("o-5", "none", "c-5", T0.plusSeconds(1)));
        all.add(cancelled);

        when(repo.findAll()).thenReturn(all);

        var stats = service.getStats();
        assertThat(stats.activeOrders()).isEqualTo(3);
        assertThat(stats.deliveredOrders()).isEqualTo(1);
        assertThat(stats.totalOrders()).isEqualTo(5);
    }

    // ── Idempotencia ─────────────────────────────────────────────────────────

    @Test
    void duplicateEvent_notPersisted() {
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", T0));
        view.apply(new OrderPreparedEvent("o-1", null, "c-1", T0.plusSeconds(4)));
        view.apply(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(8)));

        when(repo.findById("o-1")).thenReturn(Optional.of(view));

        // Primer evento ON_THE_WAY — se aplica
        service.process(new OrderOnTheWayEvent("o-1", "d-1", "c-1", T0.plusSeconds(12)));
        verify(repo, times(1)).save(eq(view), eq(Names.EVENT_ORDER_ON_THE_WAY));

        // Mismo evento duplicado — OrderView.apply() devuelve false → no se persiste
        reset(repo, broadcaster);
        when(repo.findById("o-1")).thenReturn(Optional.of(view));
        service.process(new OrderOnTheWayEvent("o-1", "d-1", "c-1", T0.plusSeconds(12)));
        verify(repo, never()).save(any(), anyString());
        verify(broadcaster, never()).broadcast(any());
    }

    // ── Transición inválida ──────────────────────────────────────────────────

    @Test
    void invalidTransition_throwsIllegalState() {
        // ASSIGNED no puede ir directamente a ARRIVING (debe pasar por ON_THE_WAY)
        OrderView view = OrderView.from(new OrderCreatedEvent("o-1", null, "c-1", T0));
        view.apply(new OrderPreparedEvent("o-1", null, "c-1", T0.plusSeconds(4)));
        view.apply(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(8)));

        when(repo.findById("o-1")).thenReturn(Optional.of(view));

        assertThatThrownBy(() ->
            service.process(new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(12)))
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("ASSIGNED");
    }
}
