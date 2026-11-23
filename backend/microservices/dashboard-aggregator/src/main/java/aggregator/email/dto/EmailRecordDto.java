package aggregator.email.dto;

import aggregator.email.database.EmailNotificationRow;

import java.time.Instant;
import java.util.UUID;

public record EmailRecordDto(
        UUID id,
        String commandId,
        String recipientEmail,
        String senderEmail,
        String subject,
        String bodyText,
        String status,
        String notificationType,
        String orderId,
        String errorMessage,
        Instant createdAt,
        Instant sentAt
) {
    public static EmailRecordDto from(EmailNotificationRow row) {
        return new EmailRecordDto(
                row.getId(),
                row.getCommandId(),
                row.getRecipientEmail(),
                row.getSenderEmail(),
                row.getSubject(),
                row.getBodyText(),
                row.getStatus(),
                row.getNotificationType(),
                row.getOrderId(),
                row.getErrorMessage(),
                row.getCreatedAt(),
                row.getSentAt()
        );
    }
}
