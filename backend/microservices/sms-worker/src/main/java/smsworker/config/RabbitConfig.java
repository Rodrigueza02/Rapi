
package smsworker.config;

import dtos.Names;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
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

    @Bean
    public Queue smsQueue() {
        return new Queue(Names.SMS_QUEUE, true);
    }

    @Bean
    public Binding smsBinding(
            Queue smsQueue,
            TopicExchange notificationsExchange) {

        return BindingBuilder
                .bind(smsQueue)
                .to(notificationsExchange)
                .with(Names.BINDING_CLIENT_ALL);
    }
}
