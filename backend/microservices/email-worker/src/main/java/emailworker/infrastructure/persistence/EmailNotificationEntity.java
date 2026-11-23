package emailworker.infrastructure.persistence;

import emailworker.domain.EmailNotificationType;
import emailworker.domain.EmailStatus;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "email_notifications",
    indexes = {
        @Index(name = "idx_email_status",     columnList = "status"),
        @Index(name = "idx_email_order_id",   columnList = "order_id"),
        @Index(name = "idx_email_created_at", columnList = "created_at")
    }
)
public class EmailNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private EmailContent content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected EmailNotificationEntity() {}

    public static EmailNotificationEntity create(
            String commandId, String recipientEmail, String senderEmail,
            String replyToEmail, String subject, String bodyText,
            EmailNotificationType notificationType, String orderId) {
        EmailNotificationEntity e = new EmailNotificationEntity();
        e.content = EmailContent.of(commandId, recipientEmail, senderEmail,
                replyToEmail, subject, bodyText, notificationType, orderId);
        e.status  = EmailStatus.PENDING;
        return e;
    }

    @PrePersist
    void prePersist() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public void markAccepted()           { status = EmailStatus.SMTP_ACCEPTED; sentAt = Instant.now(); }
    public void markFailed(String why)   { status = EmailStatus.FAILED;    errorMessage = why; }
    public void markUncertain(String why){ status = EmailStatus.UNCERTAIN;  errorMessage = why; }

    public UUID    getId()             { return id; }
    public String  getCommandId()      { return content.getCommandId(); }
    public EmailStatus getStatus()     { return status; }
    public Instant getUpdatedAt()      { return updatedAt; }
    public Instant getSentAt()         { return sentAt; }
    public Instant getCreatedAt()      { return createdAt; }
    public String  getErrorMessage()   { return errorMessage; }
    public EmailContent getContent()   { return content; }
    public String  getRecipientEmail() { return content.getRecipientEmail(); }
    public String  getSenderEmail()    { return content.getSenderEmail(); }
    public String  getReplyToEmail()   { return content.getReplyToEmail(); }
    public String  getSubject()        { return content.getSubject(); }
    public String  getBodyText()       { return content.getBodyText(); }
    public EmailNotificationType getNotificationType() { return content.getNotificationType(); }
    public String  getOrderId()        { return content.getOrderId(); }
}
