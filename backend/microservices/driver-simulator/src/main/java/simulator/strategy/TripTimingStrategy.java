package simulator.strategy;

import simulator.model.Trip;

import java.time.Duration;

public interface TripTimingStrategy {

    Duration timeToArrive(Trip trip);

    Duration timeToDeliver(Trip trip);
}