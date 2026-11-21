package dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationPayloadRecord(
        @JsonProperty("order_id") String orderId,
        @JsonProperty("recipient_id") String recipientId,
        @JsonProperty("message") String message) {

    public NotificationPayloadRecord {
        Validation.requireText(orderId, "order_id");
        Validation.requireText(recipientId, "recipient_id");
        Validation.requireText(message, "message");
    }
}