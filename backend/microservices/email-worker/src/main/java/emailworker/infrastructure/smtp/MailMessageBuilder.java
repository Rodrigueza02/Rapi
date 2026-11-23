package emailworker.infrastructure.smtp;

import emailworker.domain.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailMessageBuilder {

    private final JavaMailSender mailSender;

    public MailMessageBuilder(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public MimeMessage build(EmailMessage message) throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        boolean hasHtml = message.htmlBody() != null && !message.htmlBody().isBlank();
        MimeMessageHelper helper = new MimeMessageHelper(mime, hasHtml, "UTF-8");

        helper.setFrom(message.from());
        helper.setReplyTo(message.replyTo());
        helper.setTo(message.to());
        helper.setSubject(message.subject());

        if (hasHtml) {
            helper.setText(message.body(), message.htmlBody());
        } else {
            helper.setText(message.body(), false);
        }
        mime.setHeader("X-DeliveryFlow-Command-Id", message.commandId());
        return mime;
    }
}
