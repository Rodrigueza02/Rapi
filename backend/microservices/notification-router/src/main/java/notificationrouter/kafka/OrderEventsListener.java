
package notificationrouter.kafka;

import dtos.Names;
import dtos.OrderEvent;
import notificationrouter.service.NotificationRouterService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {

    private final NotificationRouterService service;

    public OrderEventsListener(NotificationRouterService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = Names.ORDER_EVENTS_TOPIC,
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void receive(OrderEvent event) {
        service.route(event);
    }
}
