package aggregator.dto;

import aggregator.model.OrderView;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderViewDto(
        @JsonProperty("order_id")          String     orderId,
        @JsonProperty("customer_id")       String     customerId,
        @JsonProperty("delivery_address")  String     deliveryAddress,
        @JsonProperty("total_amount")      BigDecimal totalAmount,
        @JsonProperty("items_json")        String     itemsJson,
        @JsonProperty("driver_id")         String     driverId,
        @JsonProperty("state")             String     state,
        @JsonProperty("last_updated")      String     lastUpdated
) {
    public static OrderViewDto from(OrderView v) {
        return new OrderViewDto(
                v.getOrderId(),
                v.getCustomerId(),
                v.getDeliveryAddress(),
                v.getTotalAmount(),
                v.getItemsJson(),
                v.getDriverId(),
                v.getState().name(),
                v.getLastUpdated().toString());
    }
}
