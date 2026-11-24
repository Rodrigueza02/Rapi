package processor.service;

import dtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import processor.exception.InvalidStateTransitionException;
import processor.exception.OrderNotFoundException;
import processor.model.DriverAssignment;
import processor.model.Order;
import processor.ports.OrderEventPublisherPort;
import processor.ports.OrderRepositoryPort;
import processor.state.OrderState;
import processor.strategy.DriverAssignmentStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessOrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    @Mock OrderRepositoryPort      orderRepository;
    @Mock OrderEventPublisherPort  eventPublisher;
    @Mock DriverAssignmentStrategy driverStrategy;
    @Mock Clock                    clock;

    /** Delays de 0 ms para que los asserts no esperen en tests unitarios. */
    private ProcessOrderService service;

    @BeforeEach
    void setUp() {
        service = new ProcessOrderService(
                orderRepository, eventPublisher, driverStrategy, clock,
                Duration.ZERO,   // prepareDelay
                Duration.ZERO    // assignDelay
        );
    }

    private OrderCreatedCommand cmd() {
        return new OrderCreatedCommand("order-1", "customer-1", "3001234567",
                "Calle 18 # 25-10, Pasto", List.of("Pizza"), new BigDecimal("25000"));
    }

    // ── Pedido nuevo ─────────────────────────────────────────────────────────

    @Test
    void newOrder_publishesCreatedPreparedAssigned() throws InterruptedException {
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(NOW);
        when(driverStrategy.selectDriver(any())).thenReturn("driver-7");

        service.process(cmd());

        // Con delays = 0, los callbacks se ejecutan casi de inmediato,
        // pero en el executor asíncrono. Esperamos brevemente.
        TimeUnit.MILLISECONDS.sleep(200);

        // 3 saves: CREATED, PREPARED, ASSIGNED
        verify(orderRepository, times(3)).save(any(Order.class));

        ArgumentCaptor<OrderEvent> cap = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher, times(3)).publish(cap.capture());
        List<OrderEvent> events = cap.getAllValues();
        assertThat(events.get(0)).isInstanceOf(OrderCreatedEvent.class);
        assertThat(events.get(1)).isInstanceOf(OrderPreparedEvent.class);
        assertThat(events.get(2)).isInstanceOf(DriverAssignedEvent.class);
        assertThat(((DriverAssignedEvent) events.get(2)).driverId()).isEqualTo("driver-7");
    }

    // ── Re-entrega RabbitMQ ───────────────────────────────────────────────────

    @Test
    void redeliveredCommand_republishesDriverAssignedOnly() {
        Order existing = Order.restore("order-1", "customer-1", "Calle 18",
                new BigDecimal("25000"), NOW,
                OrderState.fromName("ASSIGNED"),
                new DriverAssignment("driver-3", NOW),
                null, null, null);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(existing));

        service.process(cmd());

        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher).publish(any(DriverAssignedEvent.class));
    }

    // ── Cancelación ─────────────────────────────────────────────────────────

    @Test
    void cancelOrder_publishesCancelledEvent() {
        Order o = Order.restore("order-1", "customer-1", "Calle 18",
                new BigDecimal("25000"), NOW,
                OrderState.fromName("CREATED"), null, null, null, null);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(o));
        when(clock.instant()).thenReturn(NOW);

        service.cancelOrder("order-1");

        verify(orderRepository).save(any(Order.class));
        ArgumentCaptor<OrderEvent> cap = ArgumentCaptor.forClass(OrderEvent.class);
        verify(eventPublisher).publish(cap.capture());
        assertThat(cap.getValue()).isInstanceOf(OrderCancelledEvent.class);
    }

    @Test
    void cancelOrder_throwsWhenNotFound() {
        when(orderRepository.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelOrder("x"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void cancelOrder_throwsWhenDelivered() {
        Order o = Order.restore("order-1", "c-1", "Calle 18",
                new BigDecimal("25000"), NOW,
                OrderState.fromName("DELIVERED"),
                new DriverAssignment("d-1", NOW),
                null, null, NOW);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(o));
        assertThatThrownBy(() -> service.cancelOrder("order-1"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    // ── Validación de dependencias ───────────────────────────────────────────

    @Test
    void rejectsNullDependencies() {
        assertThatThrownBy(() -> new ProcessOrderService(
                null, eventPublisher, driverStrategy, clock, Duration.ZERO, Duration.ZERO))
                .isInstanceOf(NullPointerException.class);
    }
}
