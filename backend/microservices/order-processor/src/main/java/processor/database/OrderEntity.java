package processor.database;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id @Column(name = "order_id", nullable = false, updatable = false) private String orderId;
    @Column(name = "customer_id",      nullable = false) private String customerId;
    @Column(name = "delivery_address", nullable = false) private String deliveryAddress;
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2) private BigDecimal totalAmount;
    @Column(name = "status",           nullable = false, length = 20) private String status;
    @Column(name = "driver_id")        private String driverId;
    @Column(name = "created_at",       nullable = false) private Instant createdAt;
    @Column(name = "prepared_at")      private Instant preparedAt;
    @Column(name = "assigned_at")      private Instant assignedAt;
    @Column(name = "arrived_at")       private Instant arrivedAt;
    @Column(name = "delivered_at")     private Instant deliveredAt;
    @Column(name = "items_json",       columnDefinition = "TEXT") private String itemsJson;

    protected OrderEntity() {}

    public OrderEntity(String orderId, String customerId, String deliveryAddress,
                       BigDecimal totalAmount, String status, String driverId,
                       Instant createdAt, Instant preparedAt, Instant assignedAt,
                       Instant arrivedAt, Instant deliveredAt, String itemsJson) {
        this.orderId = orderId; this.customerId = customerId;
        this.deliveryAddress = deliveryAddress; this.totalAmount = totalAmount;
        this.status = status; this.driverId = driverId;
        this.createdAt = createdAt; this.preparedAt = preparedAt;
        this.assignedAt = assignedAt; this.arrivedAt = arrivedAt;
        this.deliveredAt = deliveredAt; this.itemsJson = itemsJson;
    }

    public String getOrderId()         { return orderId; }
    public String getCustomerId()      { return customerId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus()          { return status; }
    public String getDriverId()        { return driverId; }
    public Instant getCreatedAt()      { return createdAt; }
    public Instant getPreparedAt()     { return preparedAt; }
    public Instant getAssignedAt()     { return assignedAt; }
    public Instant getArrivedAt()      { return arrivedAt; }
    public Instant getDeliveredAt()    { return deliveredAt; }
    public String getItemsJson()       { return itemsJson; }
}
