package aggregator.controller;

import aggregator.database.OrderHistoryEntity;
import aggregator.database.OrderHistoryRepositoryAdapter;
import aggregator.dto.DashboardStatsDto;
import aggregator.dto.DriverViewDto;
import aggregator.dto.OrderViewDto;
import aggregator.dto.RoundRobinDto;
import aggregator.service.DashboardAggregatorService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST API del dashboard.
 *
 * GET /api/dashboard/orders               → lista de pedidos (estado actual)
 * GET /api/dashboard/orders/{id}          → detalle de un pedido
 * GET /api/dashboard/orders/{id}/events   → Event Store: todos los eventos del pedido
 * GET /api/dashboard/stats                → contadores para las tarjetas
 * GET /api/dashboard/round-robin          → estado del anillo Round Robin
 * GET /api/dashboard/drivers              → vista de cada repartidor
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardAggregatorController {

    private final DashboardAggregatorService      service;
    private final OrderHistoryRepositoryAdapter   eventStore;

    public DashboardAggregatorController(DashboardAggregatorService service,
                                         OrderHistoryRepositoryAdapter eventStore) {
        this.service    = service;
        this.eventStore = eventStore;
    }

    @GetMapping("/orders")
    public List<OrderViewDto> orders() {
        return service.findAll().stream().map(OrderViewDto::from).toList();
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> orderById(@PathVariable String id) {
        return service.findById(id)
                .map(view -> ResponseEntity.ok(OrderViewDto.from(view)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Devuelve la secuencia completa de eventos de un pedido en orden cronológico.
     * Permite verificar el Event Store y el replay desde el frontend o herramientas externas.
     */
    @GetMapping("/orders/{id}/events")
    public ResponseEntity<?> orderEvents(@PathVariable String id) {
        List<OrderHistoryEntity> events = eventStore.findEventsByOrderId(id);
        if (events.isEmpty()) return ResponseEntity.notFound().build();
        List<EventDto> dtos = events.stream().map(EventDto::from).toList();
        return ResponseEntity.ok(Map.of(
                "order_id",    id,
                "event_count", dtos.size(),
                "events",      dtos
        ));
    }

    @GetMapping("/stats")
    public DashboardStatsDto stats() {
        return service.getStats();
    }

    @GetMapping("/round-robin")
    public RoundRobinDto roundRobin() {
        return service.getRoundRobin();
    }

    @GetMapping("/drivers")
    public List<DriverViewDto> drivers() {
        return service.getDrivers();
    }

    // ── DTO interno para el Event Store ──────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record EventDto(
            @JsonProperty("sequence_num") int sequenceNum,
            @JsonProperty("event_type")   String eventType,
            @JsonProperty("state")        String state,
            @JsonProperty("driver_id")    String driverId,
            @JsonProperty("occurred_at")  Instant occurredAt
    ) {
        static EventDto from(OrderHistoryEntity e) {
            return new EventDto(
                    e.getSequenceNum(),
                    e.getEventType(),
                    e.getState().name(),
                    "unknown".equals(e.getDriverId()) ? null : e.getDriverId(),
                    e.getOccurredAt()
            );
        }
    }
}
