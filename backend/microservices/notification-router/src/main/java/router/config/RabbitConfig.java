package notificationrouter.config;

import dtos.Names;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(
                Names.NOTIFICATIONS_EXCHANGE,
                true,
                false
        );
    }
}
