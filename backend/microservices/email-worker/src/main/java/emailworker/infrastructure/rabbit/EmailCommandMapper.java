package emailworker.infrastructure.rabbit;

import emailworker.domain.EmailMessage;
import emailworker.domain.EmailNotificationType;
import emailworker.dto.EmailCommand;
import emailworker.infrastructure.persistence.EmailNotificationEntity;
import emailworker.infrastructure.persistence.EmailNotificationJpaRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class EmailCommandMapper {

    private final EmailNotificationJpaRepository jpa;

    public EmailCommandMapper(EmailNotificationJpaRepository jpa) {
        this.jpa = jpa;
    }

    public EmailMessage toMessage(EmailCommand cmd) {
        return new EmailMessage(
                cmd.commandId(),
                cmd.recipientEmail(),
                cmd.senderEmail(),
                cmd.replyToEmail(),
                cmd.subject(),
                cmd.bodyText(),
                cmd.htmlBody(),
                resolveType(cmd.notificationType()),
                cmd.orderId()
        );
    }

    public void persist(EmailCommand cmd) {
        if (jpa.findByCommandId(cmd.commandId()).isPresent()) return;
        try {
            jpa.save(EmailNotificationEntity.create(
                    cmd.commandId(), cmd.recipientEmail(), cmd.senderEmail(),
                    cmd.replyToEmail(), cmd.subject(), cmd.bodyText(),
                    resolveType(cmd.notificationType()), cmd.orderId()
            ));
        } catch (DataIntegrityViolationException e) {
            // Registro ya existe (creado por aggregator o concurrente) — idempotente
        }
    }

    private EmailNotificationType resolveType(String type) {
        try {
            return EmailNotificationType.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException e) {
            return EmailNotificationType.ADMIN_MANUAL;
        }
    }
}
