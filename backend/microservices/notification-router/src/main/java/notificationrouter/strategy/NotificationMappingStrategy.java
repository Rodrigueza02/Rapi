package notificationrouter.strategy;

import dtos.NotificationCommand;
import dtos.OrderEvent;

import java.util.Optional;

public interface NotificationMappingStrategy {

    boolean supports(OrderEvent event);

    Optional<NotificationCommand> map(OrderEvent event);
}
