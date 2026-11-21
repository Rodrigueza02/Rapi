package processor.model;

import org.junit.jupiter.api.Test;
import processor.exception.InvalidStateTransitionException;
import processor.state.OrderState;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    private Order newOrder() {
        return Order.create("order-1", "customer-1", "Calle 18 # 25-10, Pasto",
                new BigDecimal("25000"), NOW);
    }

    private DriverAssignment assignmentOf(String driverId) {
        return new DriverAssignment(driverId, NOW.plusSeconds(5));
    }

    @Test
    void createsOrderInCreatedState() {
        // Act
        Order order = newOrder();

        // Assert
        assertThat(order.statusName()).isEqualTo("CREATED");
        assertThat(order.assignment()).isEmpty();
    }

    @Test
    void completesTheWholeLifecycle() {
        // Arrange
        Order order = newOrder();

        // Act + Assert, paso a paso
        order.assignDriver(assignmentOf("driver-1"));
        assertThat(order.statusName()).isEqualTo("ASSIGNED");
        assertThat(order.assignment().orElseThrow().driverId()).isEqualTo("driver-1");

        order.startDelivery();
        assertThat(order.statusName()).isEqualTo("ON_THE_WAY");

        order.registerArrival(NOW.plusSeconds(60));
        assertThat(order.statusName()).isEqualTo("ON_THE_WAY");
        assertThat(order.arrivedAt()).isPresent();

        order.completeDelivery(NOW.plusSeconds(90));
        assertThat(order.statusName()).isEqualTo("DELIVERED");
        assertThat(order.deliveredAt()).isPresent();
    }

    @Test
    void rejectsDeliveryWhenOrderIsNotOnTheWay() {
        // Arrange
        Order order = newOrder();

        // Act + Assert
        assertThatThrownBy(() -> order.completeDelivery(NOW))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("CREATED");
    }

    @Test
    void rejectsAssigningTwoDrivers() {
        // Arrange
        Order order = newOrder();
        order.assignDriver(assignmentOf("driver-1"));

        // Act + Assert
        assertThatThrownBy(() -> order.assignDriver(assignmentOf("driver-2")))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThat(order.assignment().orElseThrow().driverId()).isEqualTo("driver-1");
    }

    @Test
    void failedTransitionKeepsTheOrderUntouched() {
        // Arrange
        Order order = newOrder();

        // Act
        assertThatThrownBy(() -> order.completeDelivery(NOW))
                .isInstanceOf(InvalidStateTransitionException.class);

        // Assert
        assertThat(order.statusName()).isEqualTo("CREATED");
        assertThat(order.deliveredAt()).isEmpty();
    }

    @Test
    void rejectsArrivalBeforeOrderIsOnTheWay() {
        // Arrange
        Order order = newOrder();
        order.assignDriver(assignmentOf("driver-1"));

        // Act + Assert
        assertThatThrownBy(() -> order.registerArrival(NOW))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("ASSIGNED");
    }

    @Test
    void rejectsArrivalRegisteredTwice() {
        // Arrange
        Order order = newOrder();
        order.assignDriver(assignmentOf("driver-1"));
        order.startDelivery();
        order.registerArrival(NOW.plusSeconds(60));

        // Act + Assert
        assertThatThrownBy(() -> order.registerArrival(NOW.plusSeconds(70)))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void rejectsBlankOrderId() {
        assertThatThrownBy(() -> Order.create(" ", "customer-1", "Calle 18", BigDecimal.TEN, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void rejectsNonPositiveTotal() {
        assertThatThrownBy(() -> Order.create("order-1", "customer-1", "Calle 18", BigDecimal.ZERO, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalAmount");
    }

    @Test
    void restoreRebuildsOrderInTheGivenState() {
        // Arrange
        Order order = Order.restore("order-1", "customer-1", "Calle 18", BigDecimal.TEN, NOW,
                OrderState.fromName("ON_THE_WAY"), assignmentOf("driver-9"), null, null);

        // Act
        order.completeDelivery(NOW.plusSeconds(30));

        // Assert
        assertThat(order.statusName()).isEqualTo("DELIVERED");
    }
}