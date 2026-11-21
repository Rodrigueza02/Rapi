package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("customer_phone") String customerPhone,
        @JsonProperty("delivery_address") String deliveryAddress,
        @JsonProperty("items") List<String> items,
        @JsonProperty("total_amount") BigDecimal totalAmount) {

    public CreateOrderRequest {
        Validation.requireText(customerId, "customer_id");
        Validation.requirePhone(customerPhone, "customer_phone");
        Validation.requireText(deliveryAddress, "delivery_address");
        items = Validation.requireNonEmptyTexts(items, "items");
        Validation.requirePositive(totalAmount, "total_amount");
    }
}