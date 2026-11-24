
package aggregator.config;

import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.service.DashboardAggregatorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public DashboardAggregatorService dashboardAggregatorService(
            OrderViewRepositoryPort repository,
            DashboardBroadcastPort broadcaster) {
        return new DashboardAggregatorService(repository, broadcaster);
    }
}
