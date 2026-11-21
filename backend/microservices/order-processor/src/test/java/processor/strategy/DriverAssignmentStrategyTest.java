package processor.strategy;

import org.junit.jupiter.api.Test;
import processor.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverAssignmentStrategyTest {

    private final Order order = Order.create("order-1", "customer-1", "Calle 18",
            BigDecimal.TEN, Instant.parse("2026-11-21T10:00:00Z"));

    @Test
    void roundRobinCyclesThroughTheDrivers() {
        // Arrange
        DriverAssignmentStrategy strategy = new RoundRobinDriverStrategy(List.of("d1", "d2", "d3"));

        // Act
        List<String> picks = List.of(
                strategy.selectDriver(order), strategy.selectDriver(order),
                strategy.selectDriver(order), strategy.selectDriver(order));

        // Assert
        assertThat(picks).containsExactly("d1", "d2", "d3", "d1");
    }

    @Test
    void randomAlwaysPicksAKnownDriver() {
        // Arrange
        List<String> pool = List.of("d1", "d2", "d3");
        DriverAssignmentStrategy strategy = new RandomDriverStrategy(pool, new Random(42));

        // Act + Assert
        for (int i = 0; i < 50; i++) {
            assertThat(pool).contains(strategy.selectDriver(order));
        }
    }

    @Test
    void bothStrategiesWorkThroughTheSameInterface() {
        // Arrange
        List<DriverAssignmentStrategy> strategies = List.of(
                new RoundRobinDriverStrategy(List.of("d1")),
                new RandomDriverStrategy(List.of("d1"), new Random(1)));

        // Act + Assert
        for (DriverAssignmentStrategy strategy : strategies) {
            assertThat(strategy.selectDriver(order)).isEqualTo("d1");
        }
    }

    @Test
    void roundRobinRejectsAnEmptyPool() {
        assertThatThrownBy(() -> new RoundRobinDriverStrategy(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void randomRejectsAnEmptyPool() {
        assertThatThrownBy(() -> new RandomDriverStrategy(List.of(), new Random()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}