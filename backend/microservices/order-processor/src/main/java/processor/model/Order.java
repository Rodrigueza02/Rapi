package processor.model;

import processor.exception.InvalidStateTransitionException;
import processor.state.CreatedState;
import processor.state.OrderState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public final class Order {

    private final String orderId;
    private final String customerId;
    private final String deliveryAddress;
    private final BigDecimal totalAmount;
    private final Instant createdAt;
    private final String itemsJson;
    private OrderState state;
    private DriverAssignment assignment;
    private Instant arrivedAt;
    private Instant deliveredAt;

    private Order(String orderId, String customerId, String deliveryAddress,
                  BigDecimal totalAmount, Instant createdAt,
                  OrderState state, DriverAssignment assignment,
                  Instant arrivedAt, Instant deliveredAt, String itemsJson) {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId blank");
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("customerId blank");
        if (deliveryAddress == null || deliveryAddress.isBlank()) throw new IllegalArgumentException("deliveryAddress blank");
        if (totalAmount == null || totalAmount.signum() <= 0) throw new IllegalArgumentException("totalAmount must be > 0");
        if (createdAt == null || state == null) throw new IllegalArgumentException("createdAt/state null");
        this.orderId = orderId; this.customerId = customerId;
        this.deliveryAddress = deliveryAddress; this.totalAmount = totalAmount;
        this.createdAt = createdAt; this.state = state;
        this.assignment = assignment; this.arrivedAt = arrivedAt;
        this.deliveredAt = deliveredAt; this.itemsJson = itemsJson;
    }

    public static Order create(String orderId, String customerId, String address, BigDecimal total, Instant at) {
        return new Order(orderId, customerId, address, total, at, new CreatedState(), null, null, null, null);
    }

    public static Order create(String orderId, String customerId, String address, BigDecimal total, Instant at, String items) {
        return new Order(orderId, customerId, address, total, at, new CreatedState(), null, null, null, items);
    }

    public static Order restore(String orderId, String customerId, String address, BigDecimal total,
                                Instant at, OrderState state, DriverAssignment assignment,
                                Instant arrivedAt, Instant deliveredAt) {
        return new Order(orderId, customerId, address, total, at, state, assignment, arrivedAt, deliveredAt, null);
    }

    public void assignDriver(DriverAssignment a) {
        if (a == null) throw new IllegalArgumentException("assignment null");
        this.state = state.assignDriver(); this.assignment = a;
    }
    public void startDelivery()          { this.state = state.startDelivery(); }
    public void registerArrival(Instant at) {
        if (at == null) throw new IllegalArgumentException("arrival null");
        if (arrivedAt != null) throw new InvalidStateTransitionException("arrival already registered");
        this.state = state.registerArrival(); this.arrivedAt = at;
    }
    public void completeDelivery(Instant at) {
        if (at == null) throw new IllegalArgumentException("delivery null");
        this.state = state.completeDelivery(); this.deliveredAt = at;
    }
    public void cancel() { this.state = state.cancel(); }

    public String orderId()        { return orderId; }
    public String customerId()     { return customerId; }
    public String deliveryAddress(){ return deliveryAddress; }
    public BigDecimal totalAmount(){ return totalAmount; }
    public Instant createdAt()     { return createdAt; }
    public String statusName()     { return state.name(); }
    public Optional<DriverAssignment> assignment() { return Optional.ofNullable(assignment); }
    public Optional<Instant> arrivedAt()   { return Optional.ofNullable(arrivedAt); }
    public Optional<Instant> deliveredAt() { return Optional.ofNullable(deliveredAt); }
    public Optional<String> itemsJson()    { return Optional.ofNullable(itemsJson); }
}
