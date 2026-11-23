package aggregator.kafka;

import aggregator.email.delivery.DeliveryEmailNotifier;
import aggregator.service.DashboardAggregatorService;
import dtos.Names;
import dtos.OrderDeliveredEvent;
import dtos.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {

    private final DashboardAggregatorService service;
    private final DeliveryEmailNotifier      emailNotifier;

    public OrderEventsListener(DashboardAggregatorService service,
                               DeliveryEmailNotifier emailNotifier) {
        this.service       = service;
        this.emailNotifier = emailNotifier;
    }

    @KafkaListener(
            topics = Names.ORDER_EVENTS_TOPIC,
            containerFactory = "dashboardKafkaListenerContainerFactory"
    )
    public void onOrderEvent(OrderEvent event) {
        service.process(event);
        // Correo solo al entregar, nunca en transiciones intermedias
        if (event instanceof OrderDeliveredEvent delivered) {
            emailNotifier.notifyDelivered(delivered.orderId());
        }
    }
}
