package aggregator.model;

import aggregator.state.OrderViewState;
import dtos.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class OrderView {

    private final String orderId;
    private final String customerId;
    private final String deliveryAddress;
    private final BigDecimal totalAmount;
    private final String itemsJson;
    private String driverId;
    private OrderViewState state;
    private Instant lastUpdated;

    private OrderView(String orderId, String customerId, String deliveryAddress,
                      BigDecimal totalAmount, String itemsJson,
                      String driverId, OrderViewState state, Instant lastUpdated) {
        this.orderId         = requireText(orderId,  "orderId");
        this.customerId      = customerId;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount     = totalAmount;
        this.itemsJson       = itemsJson;
        this.driverId        = driverId;
        this.state           = Objects.requireNonNull(state);
        this.lastUpdated     = Objects.requireNonNull(lastUpdated);
    }

    public static OrderView from(OrderCreatedEvent e) {
        Objects.requireNonNull(e);
        return new OrderView(e.orderId(), e.customerId(), null, null, null,
                null, OrderViewState.CREATED, e.occurredAt());
    }

    public static OrderView from(DriverAssignedEvent e) {
        Objects.requireNonNull(e);
        return new OrderView(e.orderId(), e.customerId(), null, null, null,
                e.driverId(), OrderViewState.ASSIGNED, e.occurredAt());
    }

    public static OrderView from(OrderPreparedEvent e) {
        Objects.requireNonNull(e);
        return new OrderView(e.orderId(), e.customerId(), null, null, null,
                e.driverId(), OrderViewState.PREPARED, e.occurredAt());
    }

    public static OrderView restore(String orderId, String driverId,
                                    OrderViewState state, Instant lastUpdated) {
        return new OrderView(orderId, null, null, null, null,
                driverId, state, lastUpdated);
    }

    public static OrderView restoreFull(String orderId, String customerId,
                                        String deliveryAddress, BigDecimal totalAmount,
                                        String itemsJson, String driverId,
                                        OrderViewState state, Instant lastUpdated) {
        return new OrderView(orderId, customerId, deliveryAddress, totalAmount,
                itemsJson, driverId, state, lastUpdated);
    }

    /** Aplica un evento. Devuelve true si el estado cambió. */
    public boolean apply(OrderEvent event) {
        Objects.requireNonNull(event);
        if (!orderId.equals(event.orderId()))
            throw new IllegalArgumentException("Evento de otro pedido");

        OrderViewState next = stateFrom(event);

        if (state == next && lastUpdated.equals(event.occurredAt())) return false;
        if (state == next) throw new IllegalStateException("Evento repetido con fecha distinta");

        if (!state.canTransitionTo(next))
            throw new IllegalStateException("Transición inválida: " + state + " -> " + next);

        if (event.occurredAt().isBefore(lastUpdated))
            throw new IllegalArgumentException("Evento anterior al estado actual");

        this.state       = next;
        this.lastUpdated = event.occurredAt();
        if (event.driverId() != null && !event.driverId().isBlank())
            this.driverId = event.driverId();
        return true;
    }

    private static OrderViewState stateFrom(OrderEvent e) {
        if (e instanceof OrderCreatedEvent)   return OrderViewState.CREATED;
        if (e instanceof OrderPreparedEvent)  return OrderViewState.PREPARED;
        if (e instanceof DriverAssignedEvent) return OrderViewState.ASSIGNED;
        if (e instanceof OrderOnTheWayEvent)  return OrderViewState.ON_THE_WAY;
        if (e instanceof DriverArrivingEvent) return OrderViewState.ARRIVING;
        if (e instanceof OrderDeliveredEvent) return OrderViewState.DELIVERED;
        if (e instanceof OrderCancelledEvent) return OrderViewState.CANCELLED;
        throw new IllegalArgumentException("Evento no soportado: " + e.getClass());
    }

    private static String requireText(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " vacío");
        return v;
    }

    public String getOrderId()         { return orderId; }
    public String getCustomerId()      { return customerId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getItemsJson()       { return itemsJson; }
    public String getDriverId()        { return driverId; }
    public OrderViewState getState()   { return state; }
    public Instant getLastUpdated()    { return lastUpdated; }
}
