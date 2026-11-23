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
import static org.mockito.Mockito.*;

class DashboardAggregatorServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-21T10:00:00Z");

    private OrderViewRepositoryPort repo;
    private DashboardBroadcastPort  broadcaster;
    private DashboardAggregatorService service;

    @BeforeEach void setUp() {
        repo        = mock(OrderViewRepositoryPort.class);
        broadcaster = mock(DashboardBroadcastPort.class);
        service     = new DashboardAggregatorService(repo, broadcaster);
    }

    @Test void processPreparedEvent_createsNewView() {
        when(repo.findById("o-1")).thenReturn(Optional.empty());
        service.process(new OrderPreparedEvent("o-1", null, "c-1", T0));
        verify(repo).save(any(OrderView.class));
        verify(broadcaster).broadcast(any(OrderView.class));
    }

    @Test void processDriverAssigned_updatesView() {
        OrderView view = OrderView.from(new OrderPreparedEvent("o-1", null, "c-1", T0));
        when(repo.findById("o-1")).thenReturn(Optional.of(view));
        service.process(new DriverAssignedEvent("o-1", "d-1", "c-1", T0.plusSeconds(2)));
        verify(repo).save(view);
    }

    @Test void stats_activeExcludesDeliveredAndCancelled() {
        List<OrderView> all = new ArrayList<>();
        all.add(OrderView.from(new OrderPreparedEvent("o-1", null, "c-1", T0)));           // PREPARED  → active
        OrderView assigned = OrderView.from(new OrderPreparedEvent("o-2", null, "c-2", T0));
        assigned.apply(new DriverAssignedEvent("o-2", "d-1", "c-2", T0.plusSeconds(1)));  // ASSIGNED  → active
        all.add(assigned);
        OrderView delivered = OrderView.from(new DriverAssignedEvent("o-3", "d-1", "c-3", T0));
        delivered.apply(new DriverArrivingEvent("o-3", "d-1", "c-3", T0.plusSeconds(10)));
        delivered.apply(new OrderDeliveredEvent("o-3", "d-1", "c-3", T0.plusSeconds(20)));
        all.add(delivered);                                                                 // DELIVERED → not active
        OrderView cancelled = OrderView.from(new OrderPreparedEvent("o-4", null, "c-4", T0));
        cancelled.apply(new OrderCancelledEvent("o-4", "none", "c-4", T0.plusSeconds(1)));
        all.add(cancelled);                                                                 // CANCELLED → not active
        when(repo.findAll()).thenReturn(all);

        var stats = service.getStats();
        assertThat(stats.activeOrders()).isEqualTo(2);
        assertThat(stats.deliveredOrders()).isEqualTo(1);
        assertThat(stats.totalOrders()).isEqualTo(4);
    }

    @Test void duplicateEventIgnored() {
        OrderView view = OrderView.from(new DriverAssignedEvent("o-1", "d-1", "c-1", T0));
        when(repo.findById("o-1")).thenReturn(Optional.of(view));
        // mismo evento dos veces
        service.process(new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(5)));
        reset(repo, broadcaster);
        when(repo.findById("o-1")).thenReturn(Optional.of(view));
        service.process(new DriverArrivingEvent("o-1", "d-1", "c-1", T0.plusSeconds(5)));
        verify(repo, never()).save(any());
    }
}
