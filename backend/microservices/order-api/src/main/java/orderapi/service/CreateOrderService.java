package orderapi.service;

import dtos.CreateOrderRequest;
import dtos.OrderCreatedCommand;
import orderapi.ports.OrderCommandPublisherPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateOrderService {

    private final OrderCommandPublisherPort publisher;

    public CreateOrderService(OrderCommandPublisherPort publisher) {
        this.publisher = publisher;
    }

    public String createOrder(CreateOrderRequest request) {

        String orderId = UUID.randomUUID().toString();

        OrderCreatedCommand command =
                OrderCreatedCommand.from(orderId, request);

        publisher.publish(command);

        return orderId;
    }
}
