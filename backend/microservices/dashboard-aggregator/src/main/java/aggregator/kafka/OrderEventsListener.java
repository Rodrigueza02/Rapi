package aggregator.kafka;

import aggregator.service.DashboardAggregatorService;
import dtos.Names;
import dtos.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsListener {

    private final DashboardAggregatorService service;

    public OrderEventsListener(DashboardAggregatorService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = Names.ORDER_EVENTS_TOPIC,
            containerFactory = "dashboardKafkaListenerContainerFactory"
    )
    public void onOrderEvent(OrderEvent event) {
        service.process(event);
    }
}
