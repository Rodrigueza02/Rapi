package aggregator.dto;

import aggregator.model.OrderView;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Proyección de OrderView para la API REST.
 * Convierte los tipos internos (Instant, enum) a strings JSON.
 */
public record OrderViewDto(
        @JsonProperty("order_id")     String orderId,
        @JsonProperty("driver_id")    String driverId,
        @JsonProperty("state")        String state,
        @JsonProperty("last_updated") String lastUpdated
) {
    public static OrderViewDto from(OrderView view) {
        return new OrderViewDto(
                view.getOrderId(),
                view.getDriverId(),
                view.getState().name(),
                view.getLastUpdated().toString()
        );
    }
}
