
package aggregator.service;

import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardAggregatorServiceTest {

    private OrderViewRepositoryPort repository;
    private DashboardBroadcastPort broadcaster;
    private DashboardAggregatorService service;

    private final Instant start =
            Instant.parse("2026-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        repository = mock(OrderViewRepositoryPort.class);
        broadcaster = mock(DashboardBroadcastPort.class);

        service = new DashboardAggregatorService(
                repository,
                broadcaster
        );
    }

    @Test
    void shouldCreateOrderWhenDriverIsAssigned() {

        when(repository.findById("order-1"))
                .thenReturn(Optional.empty());

        DriverAssignedEvent event =
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start
                );

        service.process(event);

        ArgumentCaptor<OrderView> captor =
                ArgumentCaptor.forClass(OrderView.class);

        verify(repository).save(captor.capture());

        OrderView saved = captor.getValue();

        assertEquals("order-1", saved.getOrderId());
        assertEquals(OrderViewState.ASSIGNED, saved.getState());

        verify(broadcaster).broadcast(saved);
    }

    @Test
    void shouldUpdateOrderWhenDriverIsArriving() {

        OrderView view = createAssignedOrder();

        when(repository.findById("order-1"))
                .thenReturn(Optional.of(view));

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60)
                );

        service.process(event);

        assertEquals(OrderViewState.ARRIVING, view.getState());

        verify(repository).save(view);
        verify(broadcaster).broadcast(view);
    }

    @Test
    void shouldMarkOrderAsDelivered() {

        OrderView view = createAssignedOrder();

        view.apply(new DriverArrivingEvent(
                "order-1",
                "driver-1", "customer-1",
                start.plusSeconds(60)
        ));

        when(repository.findById("order-1"))
                .thenReturn(Optional.of(view));

        service.process(new OrderDeliveredEvent(
                "order-1",
                "driver-1", "customer-1",
                start.plusSeconds(120)
        ));

        assertEquals(OrderViewState.DELIVERED, view.getState());

        verify(repository).save(view);
        verify(broadcaster).broadcast(view);
    }

    @Test
    void shouldNotRepeatOperationsForDuplicateEvent() {

        OrderView view = createAssignedOrder();

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60)
                );

        view.apply(event);

        when(repository.findById("order-1"))
                .thenReturn(Optional.of(view));

        service.process(event);

        verify(repository, never()).save(any());
        verify(broadcaster, never()).broadcast(any());
    }

    @Test
    void shouldRejectArrivingEventWithoutAssignedOrder() {

        when(repository.findById("order-1"))
                .thenReturn(Optional.empty());

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60)
                );

        assertThrows(
                IllegalStateException.class,
                () -> service.process(event)
        );

        verify(repository, never()).save(any());
        verify(broadcaster, never()).broadcast(any());
    }

    @Test
    void shouldReturnAllOrders() {

        OrderView view = createAssignedOrder();

        when(repository.findAll())
                .thenReturn(List.of(view));

        List<OrderView> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("order-1", result.get(0).getOrderId());
    }

    private OrderView createAssignedOrder() {
        return OrderView.from(
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start
                )
        );
    }
}

