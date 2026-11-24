package aggregator.email.service;

import aggregator.email.database.EmailInsertGateway;
import aggregator.email.dto.SendEmailRequest;
import aggregator.email.publisher.EmailCommandPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class SendEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendEmailUseCase.class);

    private final EmailCommandPublisher publisher;
    private final EmailInsertGateway insertGateway;
    private final String senderEmail;
    private final String replyToEmail;

    public SendEmailUseCase(
            EmailCommandPublisher publisher,
            EmailInsertGateway insertGateway,
            @Value("${app.email.sender:no-reply@deliveryflow.com}") String senderEmail,
            @Value("${app.email.reply-to:soporte@deliveryflow.com}") String replyToEmail) {
        this.publisher      = publisher;
        this.insertGateway  = insertGateway;
        this.senderEmail    = senderEmail;
        this.replyToEmail   = replyToEmail;
    }

    public String send(SendEmailRequest req) {
        String commandId = UUID.randomUUID().toString();
        String orderId = req.orderId() != null ? req.orderId() : "";

        insertGateway.insertPending(commandId, req.recipientEmail(), senderEmail,
                replyToEmail, req.subject(), req.bodyText(),
                "ADMIN_MANUAL", orderId);

        try {
            publisher.publish(Map.of(
                    "command_id",        commandId,
                    "recipient_email",   req.recipientEmail(),
                    "sender_email",      senderEmail,
                    "reply_to_email",    replyToEmail,
                    "subject",           req.subject(),
                    "body_text",         req.bodyText(),
                    "notification_type", "ADMIN_MANUAL",
                    "order_id",          orderId
            ));
        } catch (RuntimeException e) {
            log.error("PERSISTED pero publicación falló (queda PENDING): commandId={}", commandId, e);
        }
        return commandId;
    }
}
