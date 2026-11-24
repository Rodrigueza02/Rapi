package emailworker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailCommand(
        @JsonProperty("command_id")        String commandId,
        @JsonProperty("recipient_email")   String recipientEmail,
        @JsonProperty("sender_email")      String senderEmail,
        @JsonProperty("reply_to_email")    String replyToEmail,
        @JsonProperty("subject")           String subject,
        @JsonProperty("body_text")         String bodyText,
        @JsonProperty("html_body")         String htmlBody,
        @JsonProperty("notification_type") String notificationType,
        @JsonProperty("order_id")          String orderId
) {}
