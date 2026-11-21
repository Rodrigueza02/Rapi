package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;

@JsonTypeName(Names.EVENT_DRIVER_ARRIVING)
public record DriverArrivingEvent(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("driver_id") String driverId,
        @JsonProperty("occurred_at") Instant occurredAt) implements OrderEvent {

    public DriverArrivingEvent {
        Validation.requireText(orderId, "order_id");
        Validation.requireText(driverId, "driver_id");
        Validation.requireNonNull(occurredAt, "occurred_at");
    }
}