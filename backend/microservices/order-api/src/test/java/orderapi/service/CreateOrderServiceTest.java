package orderapi.service;

import dtos.CreateOrderRequest;
import dtos.OrderCreatedCommand;
import orderapi.ports.OrderCommandPublisherPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateOrderServiceTest {

    private OrderCommandPublisherPort publisher;
    private CreateOrderService service;

    @BeforeEach
    void setUp() {
        publisher = mock(OrderCommandPublisherPort.class);
        service = new CreateOrderService(publisher);
    }

    @Test
    void shouldCreateOrderAndPublishCommand() {

        // ARRANGE
        CreateOrderRequest request = new CreateOrderRequest(
                "customer-1",
                "+573001234567",
                "Calle 18 # 25-10, Pasto",
                List.of("Hamburguesa", "Gaseosa"),
                new BigDecimal("25000")
        );

        // ACT
        String orderId = service.createOrder(request);

        // ASSERT
        assertNotNull(orderId);
        assertFalse(orderId.isBlank());

        ArgumentCaptor<OrderCreatedCommand> captor =
                ArgumentCaptor.forClass(OrderCreatedCommand.class);

        verify(publisher, times(1)).publish(captor.capture());

        OrderCreatedCommand command = captor.getValue();

        assertEquals(orderId, command.orderId());
        assertEquals("customer-1", command.customerId());
        assertEquals(new BigDecimal("25000"), command.totalAmount());
    }

    @Test
    void shouldGenerateDifferentIdsForDifferentOrders() {

        CreateOrderRequest request = new CreateOrderRequest(
                "customer-1",
                "+573001234567",
                "Pasto, Nariño",
                List.of("Pizza"),
                new BigDecimal("30000")
        );

        String firstId = service.createOrder(request);
        String secondId = service.createOrder(request);

        assertNotEquals(firstId, secondId);
        verify(publisher, times(2)).publish(any(OrderCreatedCommand.class));
    }

    @Test
    void shouldPropagatePublisherFailure() {

        CreateOrderRequest request = new CreateOrderRequest(
                "customer-1",
                "+573001234567",
                "Pasto, Nariño",
                List.of("Pizza"),
                new BigDecimal("30000")
        );

        doThrow(new IllegalStateException("RabbitMQ unavailable"))
                .when(publisher)
                .publish(any(OrderCreatedCommand.class));

        assertThrows(
                IllegalStateException.class,
                () -> service.createOrder(request)
        );
    }
}
