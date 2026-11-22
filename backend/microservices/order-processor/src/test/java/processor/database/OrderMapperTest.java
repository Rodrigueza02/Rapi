package processor.database;

import org.junit.jupiter.api.Test;
import processor.model.DriverAssignment;
import processor.model.Order;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    @Test
    void createdOrderSurvivesTheRoundTrip() {
        // Arrange
        Order order = Order.create("order-1", "customer-1", "Calle 18", new BigDecimal("25000"), NOW);

        // Act
        Order restored = OrderMapper.toDomain(OrderMapper.toEntity(order));

        // Assert
        assertThat(restored.orderId()).isEqualTo("order-1");
        assertThat(restored.statusName()).isEqualTo("CREATED");
        assertThat(restored.assignment()).isEmpty();
    }

    @Test
    void deliveredOrderSurvivesTheRoundTrip() {
        // Arrange
        Order order = Order.create("order-1", "customer-1", "Calle 18", new BigDecimal("25000"), NOW);
        order.assignDriver(new DriverAssignment("driver-1", NOW.plusSeconds(1)));
        order.startDelivery();
        order.registerArrival(NOW.plusSeconds(60));
        order.completeDelivery(NOW.plusSeconds(90));

        // Act
        Order restored = OrderMapper.toDomain(OrderMapper.toEntity(order));

        // Assert
        assertThat(restored.statusName()).isEqualTo("DELIVERED");
        assertThat(restored.assignment().orElseThrow().driverId()).isEqualTo("driver-1");
        assertThat(restored.arrivedAt()).isPresent();
        assertThat(restored.deliveredAt()).isPresent();
    }
}