package simulator.service;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;
import dtos.OrderOnTheWayEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import simulator.model.Trip;
import simulator.ports.DriverEventPublisherPort;
import simulator.ports.TripSchedulerPort;
import simulator.strategy.TripTimingStrategy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Dobles de prueba: el temporizador es un mock, así que la prueba NO espera
 * retrasos reales: ejecutamos a mano las tareas que el servicio programó.
 */
@ExtendWith(MockitoExtension.class)
class TripSimulationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    @Mock
    private DriverEventPublisherPort eventPublisher;

    @Mock
    private TripSchedulerPort scheduler;

    @Mock
    private TripTimingStrategy timing;

    @Mock
    private Clock clock;

    @InjectMocks
    private TripSimulationService service;

    private DriverAssignedEvent assignedEvent() {
        return new DriverAssignedEvent("order-1", "driver-1", "customer-1", NOW);
    }

    @Test
    void publishesOnTheWayThenArrivingThenDeliveredWhenTimersFire() {
        // Arrange
        when(timing.timeToOnTheWay(any(Trip.class))).thenReturn(Duration.ofSeconds(4));
        when(timing.timeToArrive(any(Trip.class))).thenReturn(Duration.ofSeconds(4));
        when(timing.timeToDeliver(any(Trip.class))).thenReturn(Duration.ofSeconds(4));
        when(clock.instant()).thenReturn(NOW);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Runnable> onTheWayTask  = ArgumentCaptor.forClass(Runnable.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Runnable> arrivingTask  = ArgumentCaptor.forClass(Runnable.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Runnable> deliveryTask  = ArgumentCaptor.forClass(Runnable.class);

        // Act: trigger assignment, then fire each scheduled task in turn
        service.onDriverAssigned(assignedEvent());
        verify(scheduler).schedule(eq(Duration.ofSeconds(4)), onTheWayTask.capture());
        onTheWayTask.getValue().run();

        verify(scheduler, times(2)).schedule(eq(Duration.ofSeconds(4)), arrivingTask.capture());
        arrivingTask.getAllValues().get(1).run();

        verify(scheduler, times(3)).schedule(eq(Duration.ofSeconds(4)), deliveryTask.capture());
        deliveryTask.getAllValues().get(2).run();

        // Assert: three events published in order
        InOrder inOrder = inOrder(eventPublisher);
        inOrder.verify(eventPublisher).publish(new OrderOnTheWayEvent("order-1", "driver-1", "customer-1", NOW));
        inOrder.verify(eventPublisher).publish(new DriverArrivingEvent("order-1", "driver-1", "customer-1", NOW));
        inOrder.verify(eventPublisher).publish(new OrderDeliveredEvent("order-1", "driver-1", "customer-1", NOW));
    }

    @Test
    void publishesNothingUntilTheFirstTimerFires() {
        // Arrange
        when(timing.timeToOnTheWay(any(Trip.class))).thenReturn(Duration.ofSeconds(4));

        // Act
        service.onDriverAssigned(assignedEvent());

        // Assert
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void ignoresAnAssignedEventThatArrivesTwice() {
        // Arrange
        when(timing.timeToOnTheWay(any(Trip.class))).thenReturn(Duration.ofSeconds(4));

        // Act
        service.onDriverAssigned(assignedEvent());
        service.onDriverAssigned(assignedEvent());

        // Assert: only one trip was scheduled
        verify(scheduler, times(1)).schedule(any(Duration.class), any(Runnable.class));
    }

    @Test
    void rejectsNullDependencies() {
        assertThatThrownBy(() -> new TripSimulationService(null, scheduler, timing, clock))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventPublisher");
    }
}
