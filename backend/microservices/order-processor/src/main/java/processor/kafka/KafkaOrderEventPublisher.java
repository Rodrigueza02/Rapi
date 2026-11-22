package processor.kafka;

import dtos.Names;
import dtos.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import processor.ports.OrderEventPublisherPort;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisherPort {

    private static final long TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(OrderEvent event) {
        try {
           
            kafkaTemplate.send(Names.ORDER_EVENTS_TOPIC, event.orderId(), event)
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing the event of order " + event.orderId(), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Could not publish the event of order " + event.orderId(), e);
        }
    }
}