package aggregator.email.database;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Subselect("SELECT * FROM email_notifications")
@Immutable
public class EmailNotificationRow {

    @Id
    private UUID id;

    @Column(name = "command_id")      private String commandId;
    @Column(name = "recipient_email") private String recipientEmail;
    @Column(name = "sender_email")    private String senderEmail;
    @Column(name = "reply_to_email")   private String replyToEmail;
    @Column(name = "subject")         private String subject;
    @Column(name = "body_text")       private String bodyText;
    @Column(name = "notification_type") private String notificationType;
    @Column(name = "order_id")        private String orderId;
    @Column(name = "status")          private String status;
    @Column(name = "error_message")   private String errorMessage;
    @Column(name = "created_at")      private Instant createdAt;
    @Column(name = "updated_at")      private Instant updatedAt;
    @Column(name = "sent_at")         private Instant sentAt;

    protected EmailNotificationRow() {}

    public UUID    getId()               { return id; }
    public String  getCommandId()        { return commandId; }
    public String  getRecipientEmail()   { return recipientEmail; }
    public String  getSenderEmail()      { return senderEmail; }
    public String  getReplyToEmail()     { return replyToEmail; }
    public String  getSubject()          { return subject; }
    public String  getBodyText()         { return bodyText; }
    public String  getStatus()           { return status; }
    public String  getNotificationType() { return notificationType; }
    public String  getOrderId()          { return orderId; }
    public String  getErrorMessage()     { return errorMessage; }
    public Instant getCreatedAt()        { return createdAt; }
    public Instant getUpdatedAt()        { return updatedAt; }
    public Instant getSentAt()           { return sentAt; }
}
