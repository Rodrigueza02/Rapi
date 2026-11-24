package dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderEventTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void rejectsBlankDriverId() {
        assertThatThrownBy(() -> new DriverAssignedEvent("order-1", " ", "customer-1", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("driver_id");
    }

    @Test
    void rejectsBlankCustomerId() {
        assertThatThrownBy(() -> new DriverArrivingEvent("order-1", "driver-1", " ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customer_id");
    }

    @Test
    void rejectsMissingTimestamp() {
        assertThatThrownBy(() -> new OrderDeliveredEvent("order-1", "driver-1", "customer-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("occurred_at");
    }

    @Test
    void writesEventTypeAndSnakeCaseFields() throws Exception {
        // Arrange
        OrderEvent event = new DriverAssignedEvent("order-1", "driver-1", "customer-1",
                Instant.parse("2026-11-21T10:00:00Z"));

        // Act
        String json = mapper.writeValueAsString(event);

        // Assert
        assertThat(json)
                .contains("\"event_type\":\"DRIVER_ASSIGNED\"")
                .contains("\"order_id\":\"order-1\"")
                .contains("\"driver_id\":\"driver-1\"")
                .contains("\"customer_id\":\"customer-1\"");
    }

    @Test
    void readsTheRightRecordFromTheEventTypeField() throws Exception {
        // Arrange
        String json = """
                {"event_type":"DRIVER_ARRIVING","order_id":"order-1","driver_id":"driver-1",
                 "customer_id":"customer-1","occurred_at":"2026-11-21T10:05:00Z"}""";

        // Act
        OrderEvent event = mapper.readValue(json, OrderEvent.class);

        // Assert
        assertThat(event).isInstanceOf(DriverArrivingEvent.class);
        assertThat(event.orderId()).isEqualTo("order-1");
        assertThat(event.customerId()).isEqualTo("customer-1");
    }

    @Test
    void allSevenKnownEventsArePermitted() {
        Class<?>[] permitted = OrderEvent.class.getPermittedSubclasses();
        assertThat(OrderEvent.class.isSealed()).isTrue();
        assertThat(permitted).containsExactlyInAnyOrder(
                OrderCreatedEvent.class,
                OrderPreparedEvent.class,
                DriverAssignedEvent.class,
                OrderOnTheWayEvent.class,
                DriverArrivingEvent.class,
                OrderDeliveredEvent.class,
                OrderCancelledEvent.class);
    }

    @Test
    void writesPreparedEventType() throws Exception {
        OrderEvent event = new OrderPreparedEvent("order-1", null, "customer-1",
                Instant.parse("2026-11-21T10:00:00Z"));
        String json = mapper.writeValueAsString(event);
        assertThat(json).contains("\"event_type\":\"ORDER_PREPARED\"");
    }

    @Test
    void writesCancelledEventType() throws Exception {
        OrderEvent event = new OrderCancelledEvent("order-1", "driver-1", "customer-1",
                Instant.parse("2026-11-21T10:00:00Z"));
        String json = mapper.writeValueAsString(event);
        assertThat(json).contains("\"event_type\":\"ORDER_CANCELLED\"");
    }
}