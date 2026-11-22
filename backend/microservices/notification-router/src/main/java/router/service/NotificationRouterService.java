
package notificationrouter.service;

import dtos.NotificationCommand;
import dtos.OrderEvent;
import notificationrouter.ports.NotificationCommandPort;
import notificationrouter.strategy.NotificationMappingStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationRouterService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationRouterService.class);

    private final List<NotificationMappingStrategy> strategies;
    private final NotificationCommandPort publisher;

    public NotificationRouterService(
            List<NotificationMappingStrategy> strategies,
            NotificationCommandPort publisher) {

        this.strategies = List.copyOf(strategies);
        this.publisher = publisher;
    }

    public void route(OrderEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Order event is required"
            );
        }

        for (NotificationMappingStrategy strategy : strategies) {

            if (!strategy.supports(event)) {
                continue;
            }

            strategy.map(event).ifPresentOrElse(
                    publisher::publish,
                    () -> log.warn(
                            "Notification not published for orderId={}",
                            event.orderId()
                    )
            );

            return;
        }

        throw new IllegalArgumentException(
                "No notification strategy supports event: "
                        + event.getClass().getSimpleName()
        );
    }
}
