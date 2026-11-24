package aggregator.email.delivery;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class DeliveryOrderRow {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "items_json")
    private String itemsJson;

    @Column(name = "status")
    private String status;

    protected DeliveryOrderRow() {}

    public String getOrderId()         { return orderId; }
    public String getCustomerId()      { return customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getItemsJson()       { return itemsJson; }
    public String getStatus()          { return status; }
}
