package aggregator.database;

import aggregator.model.OrderView;
import aggregator.state.OrderViewState;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_history",
       indexes = { @Index(name = "idx_order_history_order", columnList = "order_id") })
public class OrderHistoryEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id",         nullable = false) private String orderId;
    @Column(name = "customer_id")                        private String customerId;
    @Column(name = "delivery_address")                   private String deliveryAddress;
    @Column(name = "total_amount", precision = 12, scale = 2) private BigDecimal totalAmount;
    @Column(name = "items_json",   columnDefinition = "TEXT") private String itemsJson;
    @Column(name = "driver_id",        nullable = false) private String driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)            private OrderViewState state;

    @Column(name = "occurred_at", nullable = false)      private Instant occurredAt;

    protected OrderHistoryEntity() {}

    private OrderHistoryEntity(String orderId, String customerId, String deliveryAddress,
                               BigDecimal totalAmount, String itemsJson,
                               String driverId, OrderViewState state, Instant occurredAt) {
        this.orderId = orderId; this.customerId = customerId;
        this.deliveryAddress = deliveryAddress; this.totalAmount = totalAmount;
        this.itemsJson = itemsJson; this.driverId = driverId;
        this.state = state; this.occurredAt = occurredAt;
    }

    public static OrderHistoryEntity from(OrderView v) {
        return new OrderHistoryEntity(
                v.getOrderId(), v.getCustomerId(), v.getDeliveryAddress(),
                v.getTotalAmount(), v.getItemsJson(),
                v.getDriverId() != null ? v.getDriverId() : "unknown",
                v.getState(), v.getLastUpdated());
    }

    public UUID getId()                { return id; }
    public String getOrderId()         { return orderId; }
    public String getCustomerId()      { return customerId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getItemsJson()       { return itemsJson; }
    public String getDriverId()        { return driverId; }
    public OrderViewState getState()   { return state; }
    public Instant getOccurredAt()     { return occurredAt; }
}
