package simulator.kafka;

import dtos.DriverAssignedEvent;
import dtos.Names;
import dtos.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import simulator.service.TripSimulationService;

@Component
public class OrderEventsListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsListener.class);

    private final TripSimulationService tripSimulationService;

    public OrderEventsListener(TripSimulationService tripSimulationService) {
        this.tripSimulationService = tripSimulationService;
    }

    @KafkaListener(topics = Names.ORDER_EVENTS_TOPIC)
    public void onEvent(OrderEvent event) {
        if (event instanceof DriverAssignedEvent assigned) {
            log.info("DRIVER_ASSIGNED received: order {} driver {}", assigned.orderId(), assigned.driverId());
            tripSimulationService.onDriverAssigned(assigned);
        }
    }
}