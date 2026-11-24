package processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import processor.ports.OrderEventPublisherPort;
import processor.ports.OrderRepositoryPort;
import processor.service.ProcessOrderService;
import processor.strategy.DriverAssignmentStrategy;
import processor.strategy.RoundRobinDriverStrategy;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public DriverAssignmentStrategy driverAssignmentStrategy(
            @Value("${app.drivers}") List<String> drivers) {
        return new RoundRobinDriverStrategy(drivers);
    }

    @Bean
    public ProcessOrderService processOrderService(
            OrderRepositoryPort orderRepository,
            OrderEventPublisherPort eventPublisher,
            DriverAssignmentStrategy driverStrategy,
            Clock clock,
            @Value("${app.simulation.prepare-seconds:4}") long prepareSeconds,
            @Value("${app.simulation.assign-seconds:4}")  long assignSeconds) {
        return new ProcessOrderService(
                orderRepository, eventPublisher, driverStrategy, clock,
                Duration.ofSeconds(prepareSeconds),
                Duration.ofSeconds(assignSeconds));
    }
}
