
package notificationrouter.service;

import dtos.DriverAssignedEvent;
import dtos.Names;
import dtos.NotificationCommand;
import dtos.OrderDeliveredEvent;
import dtos.OrderEvent;

import notificationrouter.ports.NotificationCommandPort;
import notificationrouter.strategy.ClientSmsStrategy;
import notificationrouter.strategy.DriverPushStrategy;
import notificationrouter.strategy.NotificationMappingStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationRouterServiceTest {

    private NotificationCommandPort publisher;
    private NotificationRouterService service;

    @BeforeEach
    void setUp() {

        publisher = mock(NotificationCommandPort.class);

        List<NotificationMappingStrategy> strategies =
                List.of(
                        new DriverPushStrategy(),
                        new ClientSmsStrategy()
                );

        service = new NotificationRouterService(
                strategies,
                publisher
        );
    }

    @Test
    void shouldPublishPushWhenDriverAssigned() {

        OrderEvent event = new DriverAssignedEvent(
                "order-001",
                "driver-001",
                Instant.now()
        );

        service.route(event);

        ArgumentCaptor<NotificationCommand> captor =
                ArgumentCaptor.forClass(NotificationCommand.class);

        verify(publisher).publish(captor.capture());

        NotificationCommand command = captor.getValue();

        assertEquals(
                Names.ROUTING_DRIVER_PUSH,
                command.routingKey()
        );

        assertEquals(
                "driver-001",
                command.payload().recipientId()
        );

        assertEquals(
                "order-001",
                command.payload().orderId()
        );
    }

    @Test
    void shouldNotPublishSmsWithoutCustomerId() {

        OrderEvent event = new OrderDeliveredEvent(
                "order-002",
                "driver-001",
                Instant.now()
        );

        service.route(event);

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldRejectNullEvent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.route(null)
        );

        verifyNoInteractions(publisher);
    }
}
