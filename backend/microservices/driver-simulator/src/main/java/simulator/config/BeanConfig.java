package simulator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import simulator.ports.DriverEventPublisherPort;
import simulator.ports.TripSchedulerPort;
import simulator.service.TripSimulationService;
import simulator.strategy.FixedTimingStrategy;
import simulator.strategy.TripTimingStrategy;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class BeanConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TripTimingStrategy tripTimingStrategy(@Value("${app.simulation.arrive-seconds}") long arriveSeconds,
                                                 @Value("${app.simulation.deliver-seconds}") long deliverSeconds) {

        return new FixedTimingStrategy(Duration.ofSeconds(arriveSeconds), Duration.ofSeconds(deliverSeconds));
    }

    @Bean
    public TripSimulationService tripSimulationService(DriverEventPublisherPort eventPublisher,
                                                       TripSchedulerPort scheduler,
                                                       TripTimingStrategy timing,
                                                       Clock clock) {
        return new TripSimulationService(eventPublisher, scheduler, timing, clock);
    }
}