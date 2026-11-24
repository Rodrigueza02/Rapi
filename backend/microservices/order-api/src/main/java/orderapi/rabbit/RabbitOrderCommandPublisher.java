package orderapi.rabbit;

import dtos.Names;
import dtos.OrderCreatedCommand;
import orderapi.ports.OrderCommandPublisherPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitOrderCommandPublisher
        implements OrderCommandPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitOrderCommandPublisher(
            RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(OrderCreatedCommand command) {
        rabbitTemplate.convertAndSend(
                "",
                Names.NEW_ORDERS_QUEUE,
                command
        );
    }
}
