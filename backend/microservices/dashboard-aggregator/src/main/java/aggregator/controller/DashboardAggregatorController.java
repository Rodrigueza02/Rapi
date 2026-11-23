package aggregator.controller;

import aggregator.dto.DashboardStatsDto;
import aggregator.dto.DriverViewDto;
import aggregator.dto.OrderViewDto;
import aggregator.dto.RoundRobinDto;
import aggregator.service.DashboardAggregatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Expone los datos del dashboard al frontend.
 *
 * GET /api/dashboard/orders          → lista de pedidos
 * GET /api/dashboard/orders/{id}     → detalle de un pedido
 * GET /api/dashboard/stats           → contadores para las 4 tarjetas
 * GET /api/dashboard/round-robin     → estado del anillo Round Robin
 * GET /api/dashboard/drivers         → vista de cada repartidor conocido
 *
 * Puerto: 8085 (dashboard-aggregator)
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardAggregatorController {

    private final DashboardAggregatorService service;

    public DashboardAggregatorController(DashboardAggregatorService service) {
        this.service = service;
    }

    @GetMapping("/orders")
    public List<OrderViewDto> orders() {
        return service.findAll()
                .stream()
                .map(OrderViewDto::from)
                .toList();
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> orderById(@PathVariable String id) {
        return service.findById(id)
                .map(view -> ResponseEntity.ok(OrderViewDto.from(view)))
                .orElse(ResponseEntity.notFound().build());
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
}
