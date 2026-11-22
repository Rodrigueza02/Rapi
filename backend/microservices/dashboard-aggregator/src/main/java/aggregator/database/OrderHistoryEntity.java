
package aggregator.database;

import aggregator.model.OrderView;
import aggregator.state.OrderViewState;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
    name = "order_history",
    indexes = {
        @Index(
            name = "idx_order_history_order",
            columnList = "order_id"
        )
    }
)
public class OrderHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderViewState state;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected OrderHistoryEntity() {
        // Constructor requerido por JPA.
    }

    private OrderHistoryEntity(
            String orderId,
            String driverId,
            OrderViewState state,
            Instant occurredAt) {

        this.orderId = orderId;
        this.driverId = driverId;
        this.state = state;
        this.occurredAt = occurredAt;
    }

    public static OrderHistoryEntity from(OrderView view) {
        return new OrderHistoryEntity(
                view.getOrderId(),
                view.getDriverId(),
                view.getState(),
                view.getLastUpdated()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDriverId() {
        return driverId;
    }

    public OrderViewState getState() {
        return state;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
