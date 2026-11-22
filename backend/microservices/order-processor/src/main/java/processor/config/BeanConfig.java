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
import java.util.List;

@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public DriverAssignmentStrategy driverAssignmentStrategy(@Value("${app.drivers}") List<String> drivers) {
        // Para cambiar la política: return new RandomDriverStrategy(drivers, new java.util.Random());
        return new RoundRobinDriverStrategy(drivers);
    }

    @Bean
    public ProcessOrderService processOrderService(OrderRepositoryPort orderRepository,
                                                   OrderEventPublisherPort eventPublisher,
                                                   DriverAssignmentStrategy driverStrategy,
                                                   Clock clock) {
        return new ProcessOrderService(orderRepository, eventPublisher, driverStrategy, clock);
    }
}