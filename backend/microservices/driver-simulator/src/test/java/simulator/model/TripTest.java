package simulator.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripTest {

    @Test
    void startsEnRoute() {
        // Act
        Trip trip = Trip.start("order-1", "driver-1");

        // Assert
        assertThat(trip.phase()).isEqualTo(Trip.Phase.EN_ROUTE);
    }

    @Test
    void goesFromEnRouteToArrivingToDelivered() {
        // Arrange
        Trip trip = Trip.start("order-1", "driver-1");

        // Act + Assert
        trip.markArriving();
        assertThat(trip.phase()).isEqualTo(Trip.Phase.ARRIVING);

        trip.markDelivered();
        assertThat(trip.phase()).isEqualTo(Trip.Phase.DELIVERED);
    }

    @Test
    void cannotBeDeliveredBeforeArriving() {
        // Arrange
        Trip trip = Trip.start("order-1", "driver-1");

        // Act + Assert
        assertThatThrownBy(trip::markDelivered)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EN_ROUTE");
    }

    @Test
    void cannotArriveTwice() {
        // Arrange
        Trip trip = Trip.start("order-1", "driver-1");
        trip.markArriving();

        // Act + Assert
        assertThatThrownBy(trip::markArriving)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsBlankIds() {
        assertThatThrownBy(() -> Trip.start(" ", "driver-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
        assertThatThrownBy(() -> Trip.start("order-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("driverId");
    }
}