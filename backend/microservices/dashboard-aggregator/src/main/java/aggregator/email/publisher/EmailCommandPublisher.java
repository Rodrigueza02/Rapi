package aggregator.email.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailCommandPublisher {

    private final RabbitTemplate rabbit;
    private final String exchange;
    private final String routingKey;

    public EmailCommandPublisher(
            RabbitTemplate rabbit,
            @Value("${app.email.exchange:email_jobs}") String exchange,
            @Value("${app.email.routing-key:email.send}") String routingKey) {
        this.rabbit     = rabbit;
        this.exchange   = exchange;
        this.routingKey = routingKey;
    }

    public void publish(Object command) {
        rabbit.convertAndSend(exchange, routingKey, command);
    }
}
