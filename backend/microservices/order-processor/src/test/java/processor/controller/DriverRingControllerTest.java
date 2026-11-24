package processor.controller;

import org.junit.jupiter.api.Test;
import processor.strategy.RoundRobinDriverStrategy;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DriverRingControllerTest {

    private final List<String> drivers = List.of("driver-1", "driver-2", "driver-3", "driver-4");

    @Test
    void driverRing_returnsDriversAndCurrentIndex() {
        RoundRobinDriverStrategy strategy = new RoundRobinDriverStrategy(drivers);
        DriverRingController controller   = new DriverRingController(strategy);

        Map<String, Object> ring = controller.driverRing();

        assertThat(ring.get("drivers")).isEqualTo(drivers);
        assertThat(ring.get("current_index")).isEqualTo(0);
        assertThat(ring.get("next_driver")).isEqualTo("driver-1");
    }

    @Test
    void driverRing_indexAdvancesAfterAssignment() {
        RoundRobinDriverStrategy strategy = new RoundRobinDriverStrategy(drivers);
        DriverRingController controller   = new DriverRingController(strategy);

        // Simular dos asignaciones
        strategy.selectDriver(null);
        strategy.selectDriver(null);

        Map<String, Object> ring = controller.driverRing();

        assertThat(ring.get("current_index")).isEqualTo(2);
        assertThat(ring.get("next_driver")).isEqualTo("driver-3");
    }

    @Test
    void driverRing_indexWrapsAround() {
        RoundRobinDriverStrategy strategy = new RoundRobinDriverStrategy(drivers);
        DriverRingController controller   = new DriverRingController(strategy);

        // 4 asignaciones → vuelve a 0
        for (int i = 0; i < 4; i++) strategy.selectDriver(null);

        Map<String, Object> ring = controller.driverRing();

        assertThat(ring.get("current_index")).isEqualTo(0);
        assertThat(ring.get("next_driver")).isEqualTo("driver-1");
    }
}
