package simulator.strategy;

import simulator.model.Trip;

import java.time.Duration;
import java.util.Random;

public class RandomTimingStrategy implements TripTimingStrategy {

    private final Duration min;
    private final Duration max;
    private final Random random;

    public RandomTimingStrategy(Duration min, Duration max, Random random) {
        if (min == null || max == null || min.isZero() || min.isNegative() || max.compareTo(min) < 0) {
            throw new IllegalArgumentException("min must be positive and max must be >= min");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.min = min;
        this.max = max;
        this.random = random;
    }

    @Override
    public Duration timeToArrive(Trip trip) {
        return pick();
    }

    @Override
    public Duration timeToDeliver(Trip trip) {
        return pick();
    }

    private Duration pick() {
        long span = max.toMillis() - min.toMillis();
        return Duration.ofMillis(min.toMillis() + Math.round(random.nextDouble() * span));
    }
}