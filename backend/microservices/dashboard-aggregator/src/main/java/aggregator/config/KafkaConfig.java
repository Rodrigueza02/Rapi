
package aggregator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.OrderEvent;

import org.apache.kafka.clients.consumer.ConsumerConfig;
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
    public ConsumerFactory<String, OrderEvent> dashboardConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}")
            String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}")
            String groupId,
            ObjectMapper objectMapper) {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        JsonDeserializer<OrderEvent> deserializer =
                new JsonDeserializer<>(OrderEvent.class, objectMapper);

        deserializer.addTrustedPackages("dtos");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
    dashboardKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderEvent> dashboardConsumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, OrderEvent>();

        factory.setConsumerFactory(dashboardConsumerFactory);

        // Procesamiento secuencial por partición.
        factory.setConcurrency(1);

        return factory;
    }
}
