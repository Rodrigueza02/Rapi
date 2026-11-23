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

    private OrderState state;
    private DriverAssignment assignment;
    private Instant arrivedAt;
    private Instant deliveredAt;

    private Order(String orderId, String customerId, String deliveryAddress,
                  BigDecimal totalAmount, Instant createdAt, OrderState state,
                  DriverAssignment assignment, Instant arrivedAt, Instant deliveredAt) {
        this.orderId = requireText(orderId, "orderId");
        this.customerId = requireText(customerId, "customerId");
        this.deliveryAddress = requireText(deliveryAddress, "deliveryAddress");
        if (totalAmount == null || totalAmount.signum() <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than zero");
        }
        this.totalAmount = totalAmount;
        if (createdAt == null || state == null) {
            throw new IllegalArgumentException("createdAt and state must not be null");
        }
        this.createdAt = createdAt;
        this.state = state;
        this.assignment = assignment;
        this.arrivedAt = arrivedAt;
        this.deliveredAt = deliveredAt;
    }


    public static Order create(String orderId, String customerId, String deliveryAddress,
                               BigDecimal totalAmount, Instant createdAt) {
        return new Order(orderId, customerId, deliveryAddress, totalAmount, createdAt,
                new CreatedState(), null, null, null);
    }

    public static Order restore(String orderId, String customerId, String deliveryAddress,
                                BigDecimal totalAmount, Instant createdAt, OrderState state,
                                DriverAssignment assignment, Instant arrivedAt, Instant deliveredAt) {
        return new Order(orderId, customerId, deliveryAddress, totalAmount, createdAt,
                state, assignment, arrivedAt, deliveredAt);
    }


    public void assignDriver(DriverAssignment newAssignment) {
        requireNonNull(newAssignment, "assignment");
        this.state = state.assignDriver();   
        this.assignment = newAssignment;
    }

    public void startDelivery() {
        this.state = state.startDelivery();
    }

    public void registerArrival(Instant at) {
        requireNonNull(at, "arrival time");
        if (arrivedAt != null) {
            throw new InvalidStateTransitionException("The arrival was already registered for this order");
        }
        this.state = state.registerArrival();
        this.arrivedAt = at;
    }

    public void completeDelivery(Instant at) {
        requireNonNull(at, "delivery time");
        this.state = state.completeDelivery();
        this.deliveredAt = at;
    }

    public void cancel() {
        this.state = state.cancel();
    }


    public String orderId() {
        return orderId;
    }

    public String customerId() {
        return customerId;
    }

    public String deliveryAddress() {
        return deliveryAddress;
    }

    public BigDecimal totalAmount() {
        return totalAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String statusName() {
        return state.name();
    }

    public Optional<DriverAssignment> assignment() {
        return Optional.ofNullable(assignment);
    }

    public Optional<Instant> arrivedAt() {
        return Optional.ofNullable(arrivedAt);
    }

    public Optional<Instant> deliveredAt() {
        return Optional.ofNullable(deliveredAt);
    }


    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return value;
    }

    private static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
    }
}