
package aggregator.database;

import aggregator.model.OrderView;
import aggregator.state.OrderViewState;

import dtos.DriverAssignedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderHistoryRepositoryAdapterTest {

    private OrderHistoryJpaRepository jpaRepository;
    private OrderHistoryRepositoryAdapter adapter;

    private final Instant start =
            Instant.parse("2026-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        jpaRepository = mock(OrderHistoryJpaRepository.class);

        adapter = new OrderHistoryRepositoryAdapter(
                jpaRepository
        );
    }

    @Test
    void shouldSaveOrderInHistoryAndMemory() {

        OrderView view = OrderView.from(
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start
                )
        );

        adapter.save(view);

        verify(jpaRepository)
                .saveAndFlush(any(OrderHistoryEntity.class));

        OrderView saved = adapter
                .findById("order-1")
                .orElseThrow();

        assertEquals("order-1", saved.getOrderId());
        assertEquals(OrderViewState.ASSIGNED, saved.getState());
    }

    @Test
    void shouldRecoverLastStateFromHistory() {

        OrderView assigned = OrderView.from(
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start
                )
        );

        OrderView delivered = OrderView.restore(
                "order-1",
                "driver-1",
                OrderViewState.DELIVERED,
                start.plusSeconds(120)
        );

        when(jpaRepository.findAllByOrderByOccurredAtAsc())
                .thenReturn(List.of(
                        OrderHistoryEntity.from(assigned),
                        OrderHistoryEntity.from(delivered)
                ));

        adapter.loadHistory();

        OrderView recovered = adapter
                .findById("order-1")
                .orElseThrow();

        assertEquals(
                OrderViewState.DELIVERED,
                recovered.getState()
        );
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {

        assertTrue(
                adapter.findById("unknown").isEmpty()
        );
    }
}
