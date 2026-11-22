package notificationrouter.strategy;

import dtos.DriverArrivingEvent;
import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;
import dtos.OrderEvent;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClientSmsStrategy
        implements NotificationMappingStrategy {

    @Override
    public boolean supports(OrderEvent event) {
        return event instanceof DriverArrivingEvent;
    }

    @Override
    public Optional<NotificationCommand> map(OrderEvent event) {

        if (!supports(event)) {
            return Optional.empty();
        }

        NotificationPayloadRecord payload =
                new NotificationPayloadRecord(
                        event.orderId(),
                        event.customerId(),
                        "Your driver is arriving"
                );

        return Optional.of(
                NotificationCommand.of(
                        Names.ROUTING_CLIENT_SMS,
                        payload
                )
        );
    }
}