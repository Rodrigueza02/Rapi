package orderapi.rabbit;

import dtos.Names;
import orderapi.ports.OrderCancelPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Envía la solicitud de cancelación al order-processor vía RabbitMQ.
 * Usa una cola dedicada para no mezclar con los comandos de creación.
 */
@Component
public class RabbitOrderCancelPublisher implements OrderCancelPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitOrderCancelPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void cancel(String orderId) {
        rabbitTemplate.convertAndSend(
                "",
                Names.CANCEL_ORDERS_QUEUE,
                Map.of("order_id", orderId)
        );
    }
}
