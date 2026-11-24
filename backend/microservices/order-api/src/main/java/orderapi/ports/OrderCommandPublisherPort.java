package orderapi.ports;

import dtos.OrderCreatedCommand;

public interface OrderCommandPublisherPort {

    void publish(OrderCreatedCommand command);
}
