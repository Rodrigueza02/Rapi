package notificationrouter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.OrderEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String servers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            ObjectMapper objectMapper) {

        Map<String, Object> properties = new HashMap<>();
        properties.put(
                "bootstrap.servers",
                servers
        );
        properties.put(
                "group.id",
                groupId
        );
        properties.put(
                "auto.offset.reset",
                "earliest"
        );
        properties.put(
                "enable.auto.commit",
                false
        );

        JsonDeserializer<OrderEvent> deserializer =
                new JsonDeserializer<>(OrderEvent.class, objectMapper, false);

        deserializer.addTrustedPackages("dtos");
        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
    orderEventKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderEvent> orderEventConsumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, OrderEvent>();

        factory.setConsumerFactory(orderEventConsumerFactory);

        return factory;
    }
}
