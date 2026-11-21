package dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "event_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DriverAssignedEvent.class, name = Names.EVENT_DRIVER_ASSIGNED),
        @JsonSubTypes.Type(value = DriverArrivingEvent.class, name = Names.EVENT_DRIVER_ARRIVING),
        @JsonSubTypes.Type(value = OrderDeliveredEvent.class, name = Names.EVENT_ORDER_DELIVERED)
})
public sealed interface OrderEvent
        permits DriverAssignedEvent, DriverArrivingEvent, OrderDeliveredEvent {

    String orderId();

    String driverId();

    Instant occurredAt();
}