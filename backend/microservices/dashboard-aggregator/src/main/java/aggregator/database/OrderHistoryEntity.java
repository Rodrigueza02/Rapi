package aggregator.database;

import aggregator.model.OrderView;
import aggregator.state.OrderViewState;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Fila del Event Store.
 * Cada instancia representa UN evento de dominio (append-only).
 * No se actualiza; solo se inserta.
 *
 * Columnas clave para Event Sourcing:
 *   event_type   — tipo canónico del evento (ORDER_CREATED, ORDER_PREPARED, …)
 *   sequence_num — número de secuencia dentro del pedido (0, 1, 2, …)
 *   occurred_at  — timestamp del evento
 *   state        — estado resultante de aplicar este evento
 */
@Entity
@Table(
    name = "order_history",
    indexes = {
        @Index(name = "idx_order_history_order",  columnList = "order_id"),
        @Index(name = "idx_order_history_replay",
               columnList = "order_id, sequence_num ASC, occurred_at ASC")
    }
)
public class OrderHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id",         nullable = false)
    private String orderId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderViewState state;

    /** Tipo canónico del evento: ORDER_CREATED, ORDER_PREPARED, etc. */
    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    /** Posición del evento en la secuencia del pedido (0-based). */
    @Column(name = "sequence_num", nullable = false)
    private int sequenceNum;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected OrderHistoryEntity() {}

    private OrderHistoryEntity(String orderId, String customerId, String deliveryAddress,
                               BigDecimal totalAmount, String itemsJson, String driverId,
                               OrderViewState state, String eventType,
                               int sequenceNum, Instant occurredAt) {
        this.orderId         = orderId;
        this.customerId      = customerId;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount     = totalAmount;
        this.itemsJson       = itemsJson;
        this.driverId        = driverId != null ? driverId : "unknown";
        this.state           = state;
        this.eventType       = eventType;
        this.sequenceNum     = sequenceNum;
        this.occurredAt      = occurredAt;
    }

    /** Crea un registro de evento a partir de la vista actual y su secuencia. */
    public static OrderHistoryEntity from(OrderView view, String eventType, int sequenceNum) {
        return new OrderHistoryEntity(
                view.getOrderId(),
                view.getCustomerId(),
                view.getDeliveryAddress(),
                view.getTotalAmount(),
                view.getItemsJson(),
                view.getDriverId(),
                view.getState(),
                eventType,
                sequenceNum,
                view.getLastUpdated()
        );
    }

    public UUID           getId()           { return id; }
    public String         getOrderId()      { return orderId; }
    public String         getCustomerId()   { return customerId; }
    public String         getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal     getTotalAmount()  { return totalAmount; }
    public String         getItemsJson()    { return itemsJson; }
    public String         getDriverId()     { return driverId; }
    public OrderViewState getState()        { return state; }
    public String         getEventType()    { return eventType; }
    public int            getSequenceNum()  { return sequenceNum; }
    public Instant        getOccurredAt()   { return occurredAt; }
}
