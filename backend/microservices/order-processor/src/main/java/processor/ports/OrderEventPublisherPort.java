package processor.ports;

import dtos.OrderEvent;

public interface OrderEventPublisherPort {

    void publish(OrderEvent event);
}