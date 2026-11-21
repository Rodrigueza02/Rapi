package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedCommand(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("customer_phone") String customerPhone,
        @JsonProperty("delivery_address") String deliveryAddress,
        @JsonProperty("items") List<String> items,
        @JsonProperty("total_amount") BigDecimal totalAmount) {

    public OrderCreatedCommand {
        Validation.requireText(orderId, "order_id");
        Validation.requireText(customerId, "customer_id");
        Validation.requirePhone(customerPhone, "customer_phone");
        Validation.requireText(deliveryAddress, "delivery_address");
        items = Validation.requireNonEmptyTexts(items, "items");
        Validation.requirePositive(totalAmount, "total_amount");
    }

    public static OrderCreatedCommand from(String orderId, CreateOrderRequest request) {
        return new OrderCreatedCommand(
                orderId,
                request.customerId(),
                request.customerPhone(),
                request.deliveryAddress(),
                request.items(),
                request.totalAmount());
    }
}