package orderapi.config;

import dtos.Names;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue newOrdersQueue() {
        return new Queue(Names.NEW_ORDERS_QUEUE, true);
    }

    @Bean
    public Queue cancelOrdersQueue() {
        return new Queue(Names.CANCEL_ORDERS_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
