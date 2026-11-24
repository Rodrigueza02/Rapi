package simulator.strategy;

import simulator.model.Trip;

import java.time.Duration;

public class FixedTimingStrategy implements TripTimingStrategy {

    private final Duration onTheWay;
    private final Duration arrive;
    private final Duration deliver;

    public FixedTimingStrategy(Duration onTheWay, Duration arrive, Duration deliver) {
        this.onTheWay = requirePositive(onTheWay, "onTheWay");
        this.arrive   = requirePositive(arrive,   "arrive");
        this.deliver  = requirePositive(deliver,  "deliver");
    }

    @Override
    public Duration timeToOnTheWay(Trip trip) {
        return onTheWay;
    }

    @Override
    public Duration timeToArrive(Trip trip) {
        return arrive;
    }

    @Override
    public Duration timeToDeliver(Trip trip) {
        return deliver;
    }

    private static Duration requirePositive(Duration value, String field) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(field + " must be a positive duration");
        }
        return value;
    }
}
