package processor.service;

import dtos.DriverAssignedEvent;
import dtos.OrderCreatedCommand;
import dtos.OrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import processor.model.DriverAssignment;
import processor.model.Order;
import processor.ports.OrderEventPublisherPort;
import processor.ports.OrderRepositoryPort;
import processor.state.OrderState;
import processor.strategy.DriverAssignmentStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessOrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private OrderEventPublisherPort eventPublisher;

    @Mock
    private DriverAssignmentStrategy driverStrategy;

    @Mock
    private Clock clock;

    @InjectMocks
    private ProcessOrderService service;

    private OrderCreatedCommand command() {
        return new OrderCreatedCommand("order-1", "customer-1", "3001234567",
                "Calle 18 # 25-10, Pasto", List.of("Pizza"), new BigDecimal("25000"));
    }

    @Test
    void savesTheNewOrderAndThenPublishesDriverAssigned() {
        // Arrange
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(NOW);
        when(driverStrategy.selectDriver(any(Order.class))).thenReturn("driver-7");

        // Act
        service.process(command());

        // Assert: se guardó PRIMERO y se publicó DESPUÉS
        InOrder inOrder = inOrder(orderRepository, eventPublisher);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        inOrder.verify(orderRepository).save(orderCaptor.capture());
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        inOrder.verify(eventPublisher).publish(eventCaptor.capture());

        assertThat(orderCaptor.getValue().statusName()).isEqualTo("ASSIGNED");
        assertThat(orderCaptor.getValue().assignment().orElseThrow().driverId()).isEqualTo("driver-7");
        assertThat(eventCaptor.getValue())
                .isEqualTo(new DriverAssignedEvent("order-1", "driver-7", NOW));
    }

    @Test
    void redeliveredCommandRepublishesTheEventWithoutCreatingAnotherOrder() {
        // Arrange: el pedido ya existe en la base de datos
        Order existing = Order.restore("order-1", "customer-1", "Calle 18", new BigDecimal("25000"), NOW,
                OrderState.fromName("ASSIGNED"), new DriverAssignment("driver-3", NOW), null, null);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(existing));

        // Act
        service.process(command());

        // Assert
        verify(orderRepository, never()).save(any(Order.class));
        verify(driverStrategy, never()).selectDriver(any(Order.class));
        verify(eventPublisher).publish(new DriverAssignedEvent("order-1", "driver-3", NOW));
    }

    @Test
    void doesNotPublishWhenPersistenceFails() {
        // Arrange
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(NOW);
        when(driverStrategy.selectDriver(any(Order.class))).thenReturn("driver-1");
        doThrow(new IllegalStateException("db down")).when(orderRepository).save(any(Order.class));

        // Act + Assert
        assertThatThrownBy(() -> service.process(command()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("db down");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsNullDependencies() {
        assertThatThrownBy(() -> new ProcessOrderService(null, eventPublisher, driverStrategy, clock))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("orderRepository");
    }
}