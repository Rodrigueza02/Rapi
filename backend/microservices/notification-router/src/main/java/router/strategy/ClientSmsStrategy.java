package notificationrouter.strategy;

import dtos.NotificationCommand;
import dtos.OrderDeliveredEvent;
import dtos.OrderEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClientSmsStrategy
        implements NotificationMappingStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(ClientSmsStrategy.class);

    @Override
    public boolean supports(OrderEvent event) {
        return event instanceof OrderDeliveredEvent;
    }

    @Override
    public Optional<NotificationCommand> map(OrderEvent event) {

        if (!supports(event)) {
            return Optional.empty();
        }

        log.warn(
                "SMS mapping pending customer lookup for orderId={}",
                event.orderId()
        );

        return Optional.empty();
    }
}
