package simulator.strategy;

import org.junit.jupiter.api.Test;
import simulator.model.Trip;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripTimingStrategyTest {

    private final Trip trip = Trip.start("order-1", "driver-1");

    @Test
    void fixedStrategyAlwaysReturnsTheConfiguredTimes() {
        // Arrange
        TripTimingStrategy strategy = new FixedTimingStrategy(Duration.ofSeconds(8), Duration.ofSeconds(6));

        // Act + Assert
        assertThat(strategy.timeToArrive(trip)).isEqualTo(Duration.ofSeconds(8));
        assertThat(strategy.timeToDeliver(trip)).isEqualTo(Duration.ofSeconds(6));
    }

    @Test
    void randomStrategyStaysInsideTheRange() {
        // Arrange
        Duration min = Duration.ofSeconds(2);
        Duration max = Duration.ofSeconds(5);
        TripTimingStrategy strategy = new RandomTimingStrategy(min, max, new Random(7));

        // Act + Assert
        for (int i = 0; i < 200; i++) {
            assertThat(strategy.timeToArrive(trip)).isBetween(min, max);
            assertThat(strategy.timeToDeliver(trip)).isBetween(min, max);
        }
    }

    @Test
    void bothStrategiesWorkThroughTheSameInterface() {
        // Arrange
        List<TripTimingStrategy> strategies = List.of(
                new FixedTimingStrategy(Duration.ofSeconds(3), Duration.ofSeconds(3)),
                new RandomTimingStrategy(Duration.ofSeconds(3), Duration.ofSeconds(3), new Random(1)));

        // Act + Assert
        for (TripTimingStrategy strategy : strategies) {
            assertThat(strategy.timeToArrive(trip)).isEqualTo(Duration.ofSeconds(3));
        }
    }

    @Test
    void rejectsInvalidDurations() {
        assertThatThrownBy(() -> new FixedTimingStrategy(Duration.ZERO, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RandomTimingStrategy(Duration.ofSeconds(5), Duration.ofSeconds(2), new Random()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}