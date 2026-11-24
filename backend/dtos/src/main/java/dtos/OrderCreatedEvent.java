package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;

@JsonTypeName(Names.EVENT_ORDER_CREATED)
public record OrderCreatedEvent(
        @JsonProperty("order_id")    String  orderId,
        @JsonProperty("driver_id")   String  driverId,
        @JsonProperty("customer_id") String  customerId,
        @JsonProperty("occurred_at") Instant occurredAt) implements OrderEvent {

    public OrderCreatedEvent {
        Validation.requireText(orderId,    "order_id");
        Validation.requireText(customerId, "customer_id");
        Validation.requireNonNull(occurredAt, "occurred_at");
        // driverId es null en creación
    }
}
