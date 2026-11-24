package emailworker.application;

import emailworker.domain.EmailStatus;
import emailworker.exception.DuplicateCommandException;
import emailworker.port.EmailStatusRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyGuard {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyGuard.class);

    private final EmailStatusRepositoryPort statusRepo;

    public IdempotencyGuard(EmailStatusRepositoryPort statusRepo) {
        this.statusRepo = statusRepo;
    }

    public void checkCanProcess(String commandId) {
        Optional<EmailStatus> maybeStatus = statusRepo.findStatusByCommandId(commandId);

        if (maybeStatus.isEmpty()) {
            log.warn("Idempotencia: commandId={} no encontrado en DB; procesando de todas formas", commandId);
            return;
        }

        EmailStatus current = maybeStatus.get();

        switch (current) {
            case PENDING, FAILED -> log.debug("Idempotencia: commandId={} en estado={}, procesando",
                    commandId, current);
            case SENDING -> {
                log.warn("Idempotencia: commandId={} ya está en SENDING, descartando", commandId);
                throw new DuplicateCommandException(commandId, current);
            }
            case SMTP_ACCEPTED -> {
                log.info("Idempotencia: commandId={} ya fue enviado (SMTP_ACCEPTED), descartando", commandId);
                throw new DuplicateCommandException(commandId, current);
            }
            case UNCERTAIN -> {
                log.warn("Idempotencia: commandId={} en UNCERTAIN, requiere resolución manual", commandId);
                throw new DuplicateCommandException(commandId, current);
            }
        }
    }
}
