package dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "event_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderPreparedEvent.class,  name = Names.EVENT_ORDER_PREPARED),
        @JsonSubTypes.Type(value = DriverAssignedEvent.class, name = Names.EVENT_DRIVER_ASSIGNED),
        @JsonSubTypes.Type(value = DriverArrivingEvent.class, name = Names.EVENT_DRIVER_ARRIVING),
        @JsonSubTypes.Type(value = OrderDeliveredEvent.class, name = Names.EVENT_ORDER_DELIVERED),
        @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = Names.EVENT_ORDER_CANCELLED)
})
public sealed interface OrderEvent
        permits OrderPreparedEvent, DriverAssignedEvent, DriverArrivingEvent, OrderDeliveredEvent, OrderCancelledEvent {

    String orderId();

    String driverId();

    String customerId();

    Instant occurredAt();
}
