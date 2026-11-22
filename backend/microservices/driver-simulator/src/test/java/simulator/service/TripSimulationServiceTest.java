package simulator.service;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;
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
 * 8 ni 6 segundos: ejecutamos a mano la tarea que el servicio programó.
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
        return new DriverAssignedEvent("order-1", "driver-1", NOW);
    }

    @Test
    void publishesArrivingAndThenDeliveredWhenTheTimersFire() {
        // Arrange
        when(timing.timeToArrive(any(Trip.class))).thenReturn(Duration.ofSeconds(8));
        when(timing.timeToDeliver(any(Trip.class))).thenReturn(Duration.ofSeconds(6));
        when(clock.instant()).thenReturn(NOW);
        ArgumentCaptor<Runnable> arrivalTask = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> deliveryTask = ArgumentCaptor.forClass(Runnable.class);

        // Act: se programa la llegada y la ejecutamos "a mano"
        service.onDriverAssigned(assignedEvent());
        verify(scheduler).schedule(eq(Duration.ofSeconds(8)), arrivalTask.capture());
        arrivalTask.getValue().run();

        // ...al llegar se programa la entrega, que también ejecutamos
        verify(scheduler).schedule(eq(Duration.ofSeconds(6)), deliveryTask.capture());
        deliveryTask.getValue().run();

        // Assert: salieron los dos eventos, en este orden
        InOrder inOrder = inOrder(eventPublisher);
        inOrder.verify(eventPublisher).publish(new DriverArrivingEvent("order-1", "driver-1", NOW));
        inOrder.verify(eventPublisher).publish(new OrderDeliveredEvent("order-1", "driver-1", NOW));
    }

    @Test
    void publishesNothingUntilTheTimersFire() {
        // Arrange
        when(timing.timeToArrive(any(Trip.class))).thenReturn(Duration.ofSeconds(8));
        when(timing.timeToDeliver(any(Trip.class))).thenReturn(Duration.ofSeconds(6));

        // Act
        service.onDriverAssigned(assignedEvent());

        // Assert
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void ignoresAnAssignedEventThatArrivesTwice() {
        // Arrange
        when(timing.timeToArrive(any(Trip.class))).thenReturn(Duration.ofSeconds(8));
        when(timing.timeToDeliver(any(Trip.class))).thenReturn(Duration.ofSeconds(6));

        // Act
        service.onDriverAssigned(assignedEvent());
        service.onDriverAssigned(assignedEvent());

        // Assert: solo se programó UN viaje
        verify(scheduler, times(1)).schedule(any(Duration.class), any(Runnable.class));
    }

    @Test
    void rejectsNullDependencies() {
        assertThatThrownBy(() -> new TripSimulationService(null, scheduler, timing, clock))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventPublisher");
    }
}