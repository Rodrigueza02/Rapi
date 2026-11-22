package simulator.service;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;
import simulator.model.Trip;
import simulator.ports.DriverEventPublisherPort;
import simulator.ports.TripSchedulerPort;
import simulator.strategy.TripTimingStrategy;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class TripSimulationService {

    private final DriverEventPublisherPort eventPublisher;
    private final TripSchedulerPort scheduler;
    private final TripTimingStrategy timing;
    private final Clock clock;

    /** Pedidos cuyo viaje ya empezó: evita simular dos veces el mismo pedido. */
    private final Set<String> startedOrders = ConcurrentHashMap.newKeySet();

    public TripSimulationService(DriverEventPublisherPort eventPublisher,
                                 TripSchedulerPort scheduler,
                                 TripTimingStrategy timing,
                                 Clock clock) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.timing = Objects.requireNonNull(timing, "timing");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void onDriverAssigned(DriverAssignedEvent event) {
        Objects.requireNonNull(event, "event");

        if (!startedOrders.add(event.orderId())) {
            return;
        }

        Trip trip = Trip.start(event.orderId(), event.driverId(), event.customerId());
        Duration toArrive = timing.timeToArrive(trip);
        Duration toDeliver = timing.timeToDeliver(trip);

        scheduler.schedule(toArrive, () -> {
            arrive(trip);
            scheduler.schedule(toDeliver, () -> deliver(trip));
        });
    }

    private void arrive(Trip trip) {
        trip.markArriving();
        eventPublisher.publish(new DriverArrivingEvent(
            trip.orderId(), trip.driverId(), trip.customerId(), clock.instant()));    }

    private void deliver(Trip trip) {
        trip.markDelivered();
        eventPublisher.publish(new OrderDeliveredEvent(
                trip.orderId(), trip.driverId(), trip.customerId(), clock.instant()));    }
}