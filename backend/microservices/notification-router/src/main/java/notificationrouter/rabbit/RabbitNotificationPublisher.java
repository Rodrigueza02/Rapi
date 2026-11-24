package notificationrouter.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dtos.Names;
import dtos.NotificationCommand;
import notificationrouter.ports.NotificationCommandPort;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitNotificationPublisher
        implements NotificationCommandPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitNotificationPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(NotificationCommand command) {

        try {
            byte[] body = objectMapper.writeValueAsBytes(command);

            Message message = MessageBuilder
                    .withBody(body)
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();

            rabbitTemplate.send(
                    Names.NOTIFICATIONS_EXCHANGE,
                    command.routingKey(),
                    message
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not serialize notification command",
                    exception
            );
        }
    }
}
