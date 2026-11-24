package aggregator.database;

import aggregator.model.OrderView;
import aggregator.state.OrderViewState;
import dtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderHistoryRepositoryAdapterTest {

    private OrderHistoryJpaRepository     jpaRepository;
    private OrderHistoryRepositoryAdapter adapter;

    private final Instant start = Instant.parse("2026-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        jpaRepository = mock(OrderHistoryJpaRepository.class);

        when(jpaRepository.findTopByOrderIdOrderBySequenceNumDesc(any()))
                .thenReturn(Optional.empty());
        when(jpaRepository.findAllByOrderByOrderIdAscSequenceNumAscOccurredAtAsc())
                .thenReturn(List.of());

        // Constructor ya no necesita DashboardAggregatorService
        adapter = new OrderHistoryRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_persistsEventAndUpdatesProjection() {
        OrderView view = OrderView.from(
                new DriverAssignedEvent("order-1", "driver-1", "customer-1", start));

        adapter.save(view, Names.EVENT_DRIVER_ASSIGNED);

        verify(jpaRepository).saveAndFlush(any(OrderHistoryEntity.class));
        OrderView saved = adapter.findById("order-1").orElseThrow();
        assertEquals("order-1", saved.getOrderId());
        assertEquals(OrderViewState.ASSIGNED, saved.getState());
    }

    @Test
    void loadHistory_reconstructsCurrentStateFromEvents() {
        OrderView assigned = OrderView.from(
                new DriverAssignedEvent("order-1", "driver-1", "customer-1", start));
        OrderView delivered = OrderView.restoreFull(
                "order-1", "customer-1", null, null, null,
                "driver-1", OrderViewState.DELIVERED, start.plusSeconds(120));

        when(jpaRepository.findAllByOrderByOrderIdAscSequenceNumAscOccurredAtAsc())
                .thenReturn(List.of(
                        OrderHistoryEntity.from(assigned, Names.EVENT_DRIVER_ASSIGNED, 0),
                        OrderHistoryEntity.from(delivered, Names.EVENT_ORDER_DELIVERED, 1)
                ));

        adapter.loadHistory();

        // El adaptador ya no llama al servicio — expone el conteo para que BeanConfig lo use
        assertEquals(1, adapter.countAssignedEvents());

        OrderView recovered = adapter.findById("order-1").orElseThrow();
        assertEquals(OrderViewState.DELIVERED, recovered.getState());
    }

    @Test
    void countAssignedEvents_returnsZeroWhenNoHistory() {
        assertEquals(0, adapter.countAssignedEvents());
    }

    @Test
    void findById_returnsEmptyForUnknownOrder() {
        assertTrue(adapter.findById("unknown").isEmpty());
    }

    @Test
    void duplicateEvent_notPersistedTwice() {
        OrderView view = OrderView.from(
                new DriverAssignedEvent("order-1", "driver-1", "customer-1", start));

        OrderHistoryEntity existing =
                OrderHistoryEntity.from(view, Names.EVENT_DRIVER_ASSIGNED, 0);
        when(jpaRepository.findTopByOrderIdOrderBySequenceNumDesc("order-1"))
                .thenReturn(Optional.of(existing));

        adapter.save(view, Names.EVENT_DRIVER_ASSIGNED);

        verify(jpaRepository, atMostOnce()).saveAndFlush(any());
    }
}
