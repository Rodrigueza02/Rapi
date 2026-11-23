package emailworker.infrastructure.smtp;

import emailworker.domain.EmailMessage;
import emailworker.exception.EmailSendException;
import emailworker.port.EmailSenderPort;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class GmailSmtpAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(GmailSmtpAdapter.class);

    private final JavaMailSender mailSender;
    private final MailMessageBuilder messageBuilder;
    private final SmtpFailureClassifier classifier;

    public GmailSmtpAdapter(JavaMailSender mailSender,
                             MailMessageBuilder messageBuilder,
                             SmtpFailureClassifier classifier) {
        this.mailSender     = mailSender;
        this.messageBuilder = messageBuilder;
        this.classifier     = classifier;
    }

    @Override
    public void send(EmailMessage message) throws EmailSendException {
        MimeMessage mime = buildMessage(message);
        dispatch(message, mime);
    }

    private MimeMessage buildMessage(EmailMessage message) throws EmailSendException {
        try {
            return messageBuilder.build(message);
        } catch (MessagingException e) {
            throw new EmailSendException(message.commandId(),
                    "Error construyendo MimeMessage: " + e.getMessage(), e, true);
        }
    }

    private void dispatch(EmailMessage message, MimeMessage mime) throws EmailSendException {
        try {
            mailSender.send(mime);
            log.debug("SMTP aceptó el mensaje: commandId={}", message.commandId());

        } catch (MailAuthenticationException e) {
            log.error("Error de autenticación SMTP: commandId={}", message.commandId());
            throw new EmailSendException(message.commandId(),
                    "Error de autenticación SMTP — verificar App Password", e, true);

        } catch (MailParseException e) {
            log.error("Mensaje malformado: commandId={}", message.commandId());
            throw new EmailSendException(message.commandId(),
                    "Mensaje SMTP malformado: " + e.getMessage(), e, true);

        } catch (MailSendException e) {
            boolean permanent = classifier.isPermanent(e);
            String desc = permanent
                    ? "SMTP rechazó el mensaje definitivamente"
                    : "Error transitorio o timeout SMTP — resultado incierto";
            log.warn("{}: commandId={}", desc, message.commandId());
            throw new EmailSendException(message.commandId(),
                    desc + ": " + e.getMessage(), e, permanent);

        } catch (Exception e) {
            log.error("Error inesperado: commandId={}", message.commandId(), e);
            throw new EmailSendException(message.commandId(),
                    "Error inesperado: " + e.getClass().getSimpleName(), e, false);
        }
    }
}
