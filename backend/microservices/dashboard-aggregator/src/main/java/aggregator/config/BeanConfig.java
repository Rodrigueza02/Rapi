package aggregator.config;

import aggregator.database.OrderHistoryRepositoryAdapter;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.service.DashboardAggregatorService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class BeanConfig {

    private static final Logger log = LoggerFactory.getLogger(BeanConfig.class);

    @Bean
    public DashboardAggregatorService dashboardAggregatorService(
            OrderViewRepositoryPort repository,
            DashboardBroadcastPort broadcaster,
            @Value("${app.drivers}") List<String> drivers) {
        return new DashboardAggregatorService(repository, broadcaster, drivers);
    }

    /**
     * Inicializa el índice Round Robin del aggregator una vez que todos los beans
     * están listos ({@code ContextRefreshedEvent}).
     *
     * <p><strong>Estrategia de sincronización:</strong>
     * <ol>
     *   <li>Consulta {@code GET /internal/driver-ring} en el order-processor para
     *       obtener el índice real del {@code AtomicInteger} en memoria.</li>
     *   <li>Si el processor no responde (no arrancó aún, red no disponible), usa
     *       como fallback el conteo de eventos DRIVER_ASSIGNED del Event Store.</li>
     * </ol>
     *
     * <p>El flag {@code seeded} evita la doble ejecución en contextos con hijos.
     */
    @Bean
    public ApplicationListener<ContextRefreshedEvent> roundRobinSeeder(
            DashboardAggregatorService service,
            OrderHistoryRepositoryAdapter adapter,
            @Value("${order-processor.driver-ring-url}") String driverRingUrl,
            ObjectMapper objectMapper) {

        AtomicBoolean seeded = new AtomicBoolean(false);

        return event -> {
            if (!seeded.compareAndSet(false, true)) return;

            int index = fetchIndexFromProcessor(driverRingUrl, objectMapper);

            if (index >= 0) {
                log.info("Round Robin sincronizado con order-processor: índice={}", index);
                service.seedRoundRobinFromIndex(index);
            } else {
                int historicalCount = adapter.countAssignedEvents();
                log.warn("order-processor no disponible. Usando historial: {} asignaciones.", historicalCount);
                service.seedRoundRobinFromHistory(historicalCount);
            }
        };
    }

    /**
     * Consulta el endpoint {@code /internal/driver-ring} del order-processor.
     *
     * @return el {@code current_index} real, o {@code -1} si falla la llamada.
     */
    private int fetchIndexFromProcessor(String url, ObjectMapper mapper) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode body = mapper.readTree(response.body());
                return body.path("current_index").asInt(-1);
            }
            log.warn("order-processor respondió HTTP {}", response.statusCode());
        } catch (Exception e) {
            log.warn("No se pudo contactar order-processor ({}): {}", url, e.getMessage());
        }
        return -1;
    }
}
