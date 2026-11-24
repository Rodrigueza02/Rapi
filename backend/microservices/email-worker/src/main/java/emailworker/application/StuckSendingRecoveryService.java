package emailworker.application;

import emailworker.port.EmailStatusRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class StuckSendingRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(StuckSendingRecoveryService.class);

    private final EmailStatusRepositoryPort statusRepo;
    private final int stuckMinutes;

    public StuckSendingRecoveryService(
            EmailStatusRepositoryPort statusRepo,
            @Value("${app.email.stuck-sending-minutes:5}") int stuckMinutes) {
        this.statusRepo   = statusRepo;
        this.stuckMinutes = stuckMinutes;
    }

    @Scheduled(fixedDelayString = "${app.email.recovery-interval-ms:120000}")
    public void recoverStuckSending() {
        Instant threshold = Instant.now().minus(stuckMinutes, ChronoUnit.MINUTES);
        String reason = "Proceso interrumpido: registro en SENDING durante más de "
                + stuckMinutes + " minutos. Verificar manualmente en Gmail.";

        int updated = statusRepo.recoverStuckSending(threshold, reason);

        if (updated > 0) {
            log.warn("Recovery: {} registro(s) SENDING bloqueados → UNCERTAIN (umbral: {} min)",
                    updated, stuckMinutes);
        } else {
            log.debug("Recovery: sin registros SENDING bloqueados");
        }
    }
}
