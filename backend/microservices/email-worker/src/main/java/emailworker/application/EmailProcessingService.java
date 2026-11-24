package emailworker.application;

import emailworker.domain.EmailMessage;
import emailworker.domain.EmailStatus;
import emailworker.exception.EmailSendException;
import emailworker.port.EmailSenderPort;
import emailworker.port.EmailStatusRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EmailProcessingService.class);

    private final EmailSenderPort sender;
    private final EmailStatusRepositoryPort statusRepo;

    public EmailProcessingService(EmailSenderPort sender,
                                  EmailStatusRepositoryPort statusRepo) {
        this.sender     = sender;
        this.statusRepo = statusRepo;
    }

    public void process(EmailMessage message) {
        String commandId = message.commandId();

        boolean locked = statusRepo.transitionStatus(commandId, EmailStatus.PENDING, EmailStatus.SENDING);

        if (!locked) {
            locked = statusRepo.transitionStatus(commandId, EmailStatus.FAILED, EmailStatus.SENDING);
        }

        if (!locked) {
            log.warn("No se pudo adquirir lock SENDING para commandId={}; descartando", commandId);
            throw new IllegalStateException("No se pudo transicionar a SENDING para commandId=" + commandId);
        }

        log.info("Iniciando envío: commandId={} to={}", commandId, mask(message.to()));

        try {
            sender.send(message);
            statusRepo.transitionStatus(commandId, EmailStatus.SENDING, EmailStatus.SMTP_ACCEPTED);
            statusRepo.markSentAt(commandId);
            log.info("Envío aceptado por SMTP: commandId={}", commandId);

        } catch (EmailSendException e) {
            handleSendFailure(commandId, e);
        } catch (Exception e) {
            String reason = "Excepción inesperada durante el envío: "
                    + e.getClass().getSimpleName() + ": " + sanitize(e.getMessage());
            log.error("Resultado incierto para commandId={}: {}", commandId, reason, e);
            statusRepo.markUncertain(commandId, reason);
            throw new RuntimeException("Resultado incierto: " + commandId, e);
        }
    }

    private void handleSendFailure(String commandId, EmailSendException e) {
        if (e.isPermanent()) {
            String errorMsg = sanitize(e.getMessage());
            log.warn("SMTP rechazó definitivamente: commandId={} error={}", commandId, errorMsg);
            statusRepo.transitionStatus(commandId, EmailStatus.SENDING, EmailStatus.FAILED);
            statusRepo.recordError(commandId, errorMsg);
        } else {
            String reason = "Error transitorio/timeout SMTP: " + sanitize(e.getMessage());
            log.warn("Resultado incierto (error no permanente): commandId={} reason={}", commandId, reason);
            statusRepo.markUncertain(commandId, reason);
            throw new RuntimeException("Resultado incierto: " + commandId, e);
        }
    }

    private static String mask(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static String sanitize(String message) {
        if (message == null) return "sin descripción";
        return message.replaceAll("(?i)(password|secret|token|key)=[^\\s,]+", "$1=***");
    }
}
