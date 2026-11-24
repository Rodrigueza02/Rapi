package simulator.service;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;
import dtos.OrderOnTheWayEvent;
import simulator.model.Trip;
import simulator.ports.DriverEventPublisherPort;
import simulator.ports.TripSchedulerPort;
import simulator.strategy.TripTimingStrategy;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ciclo completo:
 *   DRIVER_ASSIGNED → (delay onTheWay) → ORDER_ON_THE_WAY
 *                   → (delay arrive)   → DRIVER_ARRIVING
 *                   → (delay deliver)  → ORDER_DELIVERED
 */
public class TripSimulationService {

    private final DriverEventPublisherPort eventPublisher;
    private final TripSchedulerPort        scheduler;
    private final TripTimingStrategy       timing;
    private final Clock                    clock;

    private final Set<String> startedOrders = ConcurrentHashMap.newKeySet();

    public TripSimulationService(DriverEventPublisherPort eventPublisher,
                                 TripSchedulerPort scheduler,
                                 TripTimingStrategy timing,
                                 Clock clock) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.scheduler      = Objects.requireNonNull(scheduler,      "scheduler");
        this.timing         = Objects.requireNonNull(timing,         "timing");
        this.clock          = Objects.requireNonNull(clock,          "clock");
    }

    public void onDriverAssigned(DriverAssignedEvent event) {
        Objects.requireNonNull(event, "event");
        // Idempotente: evita simular dos veces el mismo pedido
        if (!startedOrders.add(event.orderId())) return;

        Trip trip = Trip.start(event.orderId(), event.driverId(), event.customerId());

        Duration toOnTheWay = timing.timeToOnTheWay(trip);
        Duration toArrive   = timing.timeToArrive(trip);
        Duration toDeliver  = timing.timeToDeliver(trip);

        // 1. Después de timeToOnTheWay → ORDER_ON_THE_WAY
        scheduler.schedule(toOnTheWay, () -> {
            onTheWay(trip);
            // 2. Después de timeToArrive → DRIVER_ARRIVING
            scheduler.schedule(toArrive, () -> {
                arrive(trip);
                // 3. Después de timeToDeliver → ORDER_DELIVERED
                scheduler.schedule(toDeliver, () -> deliver(trip));
            });
        });
    }

    private void onTheWay(Trip trip) {
        eventPublisher.publish(new OrderOnTheWayEvent(
                trip.orderId(), trip.driverId(), trip.customerId(), clock.instant()));
    }

    private void arrive(Trip trip) {
        trip.markArriving();
        eventPublisher.publish(new DriverArrivingEvent(
                trip.orderId(), trip.driverId(), trip.customerId(), clock.instant()));
    }

    private void deliver(Trip trip) {
        trip.markDelivered();
        eventPublisher.publish(new OrderDeliveredEvent(
                trip.orderId(), trip.driverId(), trip.customerId(), clock.instant()));
    }
}
