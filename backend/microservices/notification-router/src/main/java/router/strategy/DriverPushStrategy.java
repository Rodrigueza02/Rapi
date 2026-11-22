package notificationrouter.strategy;

import dtos.DriverAssignedEvent;
import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;
import dtos.OrderDeliveredEvent;
import dtos.OrderEvent;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DriverPushStrategy
        implements NotificationMappingStrategy {

    @Override
    public boolean supports(OrderEvent event) {
        return event instanceof DriverAssignedEvent
                || event instanceof OrderDeliveredEvent;
    }

    @Override
    public Optional<NotificationCommand> map(OrderEvent event) {

        if (!supports(event)) {
            return Optional.empty();
        }

        String message = event instanceof DriverAssignedEvent
                ? "New delivery assigned"
                : "Delivery completed";

        NotificationPayloadRecord payload =
                new NotificationPayloadRecord(
                        event.orderId(),
                        event.driverId(),
                        message
                );

        return Optional.of(
                NotificationCommand.of(
                        Names.ROUTING_DRIVER_PUSH,
                        payload
                )
        );
    }
}