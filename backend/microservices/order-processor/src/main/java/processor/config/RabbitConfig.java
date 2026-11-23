package processor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.Names;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue newOrdersQueue() {
        return QueueBuilder.durable(Names.NEW_ORDERS_QUEUE).build();
    }

    @Bean
    public Queue cancelOrdersQueue() {
        return QueueBuilder.durable(Names.CANCEL_ORDERS_QUEUE).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}