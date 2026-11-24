package aggregator.email.service;

import aggregator.email.database.EmailNotificationQueryRepository;
import aggregator.email.database.EmailNotificationRow;
import aggregator.email.dto.EmailRecordDto;
import aggregator.email.dto.EmailStatsDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmailAdminService {

    private final EmailNotificationQueryRepository repo;

    public EmailAdminService(EmailNotificationQueryRepository repo) {
        this.repo = repo;
    }

    public Page<EmailRecordDto> findAll(String status, Pageable pageable) {
        Page<EmailNotificationRow> page = (status != null && !status.isBlank())
                ? repo.findByStatusOrderByCreatedAtDesc(status.toUpperCase(), pageable)
                : repo.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(EmailRecordDto::from);
    }

    public Optional<EmailRecordDto> findById(String commandId) {
        return repo.findByCommandId(commandId).map(EmailRecordDto::from);
    }

    public List<EmailRecordDto> findByOrderId(String orderId) {
        return repo.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream().map(EmailRecordDto::from).toList();
    }

    public EmailStatsDto getStats() {
        Map<String, Long> counts = repo.countByStatus().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1]
                ));
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new EmailStatsDto(
                total,
                counts.getOrDefault("PENDING", 0L),
                counts.getOrDefault("SENDING", 0L),
                counts.getOrDefault("SMTP_ACCEPTED", 0L),
                counts.getOrDefault("FAILED", 0L),
                counts.getOrDefault("UNCERTAIN", 0L)
        );
    }
}
