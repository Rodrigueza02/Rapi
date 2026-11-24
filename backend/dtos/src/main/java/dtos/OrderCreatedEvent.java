package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Evento publicado en Kafka cuando un pedido es creado.
 *
 * <p>Incluye {@code delivery_address}, {@code items} y {@code total_amount} para
 * que el dashboard-aggregator pueda mostrar esos campos sin depender de un segundo
 * servicio. El driver_id es null en el momento de creación.
 */
@JsonTypeName(Names.EVENT_ORDER_CREATED)
public record OrderCreatedEvent(
        @JsonProperty("order_id")          String     orderId,
        @JsonProperty("driver_id")         String     driverId,
        @JsonProperty("customer_id")       String     customerId,
        @JsonProperty("delivery_address")  String     deliveryAddress,
        @JsonProperty("items")             List<String> items,
        @JsonProperty("total_amount")      BigDecimal totalAmount,
        @JsonProperty("occurred_at")       Instant    occurredAt) implements OrderEvent {

    public OrderCreatedEvent {
        Validation.requireText(orderId,    "order_id");
        Validation.requireText(customerId, "customer_id");
        Validation.requireNonNull(occurredAt, "occurred_at");
        // driverId es null en creación
        // deliveryAddress, items, totalAmount son opcionales para compatibilidad
        // con eventos ya almacenados en Kafka (campo ausente → null al deserializar)
    }
}
