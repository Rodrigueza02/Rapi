package simulator.ports;

import dtos.OrderEvent;

public interface DriverEventPublisherPort {

    void publish(OrderEvent event);
}