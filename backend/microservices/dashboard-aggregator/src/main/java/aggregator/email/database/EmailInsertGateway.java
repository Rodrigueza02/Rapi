package aggregator.email.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class EmailInsertGateway {

    private final JdbcTemplate jdbc;

    public EmailInsertGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean insertPending(String commandId, String recipient, String sender,
                                  String replyTo, String subject, String bodyText,
                                  String type, String orderId) {
        int rows = jdbc.update("""
            INSERT INTO email_notifications
                (id, command_id, recipient_email, sender_email, reply_to_email,
                 subject, body_text, notification_type, order_id, status,
                 created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', NOW(), NOW())
            ON CONFLICT (command_id) DO NOTHING
            """,
                UUID.randomUUID(), commandId, recipient, sender, replyTo,
                subject, bodyText, type, orderId != null ? orderId : null);
        return rows > 0;
    }
}
