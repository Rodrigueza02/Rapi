package emailworker.infrastructure.persistence;

import emailworker.domain.EmailNotificationType;

import jakarta.persistence.*;

@Embeddable
public class EmailContent {

    @Column(name = "command_id",      nullable = false, unique = true, length = 50)
    private String commandId;

    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @Column(name = "sender_email",    nullable = false, length = 150)
    private String senderEmail;

    @Column(name = "reply_to_email",  nullable = false, length = 150)
    private String replyToEmail;

    @Column(name = "subject",         nullable = false, length = 255)
    private String subject;

    @Column(name = "body_text",       nullable = false, columnDefinition = "TEXT")
    private String bodyText;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private EmailNotificationType notificationType;

    @Column(name = "order_id", length = 50)
    private String orderId;

    protected EmailContent() {}

    public static EmailContent of(String commandId, String recipientEmail,
            String senderEmail, String replyToEmail, String subject,
            String bodyText, EmailNotificationType notificationType, String orderId) {
        EmailContent c = new EmailContent();
        c.commandId        = commandId;
        c.recipientEmail   = recipientEmail;
        c.senderEmail      = senderEmail;
        c.replyToEmail     = replyToEmail;
        c.subject          = subject;
        c.bodyText         = bodyText;
        c.notificationType = notificationType;
        c.orderId          = orderId;
        return c;
    }

    public String  getCommandId()      { return commandId; }
    public String  getRecipientEmail() { return recipientEmail; }
    public String  getSenderEmail()    { return senderEmail; }
    public String  getReplyToEmail()   { return replyToEmail; }
    public String  getSubject()        { return subject; }
    public String  getBodyText()       { return bodyText; }
    public EmailNotificationType getNotificationType() { return notificationType; }
    public String  getOrderId()        { return orderId; }
}
