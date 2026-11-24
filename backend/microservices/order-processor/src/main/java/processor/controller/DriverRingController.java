package processor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import processor.strategy.DriverAssignmentStrategy;
import processor.strategy.RoundRobinDriverStrategy;

import java.util.List;
import java.util.Map;

/**
 * Expone el estado actual del anillo Round Robin del order-processor.
 *
 * <p>GET /internal/driver-ring
 * <pre>
 * {
 *   "drivers":       ["driver-1", "driver-2", "driver-3", "driver-4"],
 *   "current_index": 2,
 *   "next_driver":   "driver-3"
 * }
 * </pre>
 *
 * <p>El dashboard-aggregator consulta este endpoint al arrancar para
 * sincronizar su propio índice con el contador real del processor, evitando
 * la divergencia que ocurría cuando el aggregator reiniciaba y recalculaba
 * el índice solo a partir del historial persistido.
 *
 * <p>Este endpoint es interno (no requiere autenticación JWT) porque solo
 * lo consume el aggregator dentro de la red Docker. Si en el futuro se expone
 * públicamente, debe protegerse.
 */
@RestController
public class DriverRingController {

    private final DriverAssignmentStrategy strategy;

    public DriverRingController(DriverAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    @GetMapping("/internal/driver-ring")
    public Map<String, Object> driverRing() {
        if (strategy instanceof RoundRobinDriverStrategy rr) {
            List<String> drivers = rr.getDriverIds();
            int idx              = rr.currentIndex();
            String next          = drivers.isEmpty() ? null : drivers.get(idx);
            return Map.of(
                    "drivers",       drivers,
                    "current_index", idx,
                    "next_driver",   next != null ? next : ""
            );
        }
        return Map.of("drivers", List.of(), "current_index", 0, "next_driver", "");
    }
}
