package aggregator.email.database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailNotificationQueryRepository
        extends JpaRepository<EmailNotificationRow, UUID> {

    Page<EmailNotificationRow> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<EmailNotificationRow> findByStatusOrderByCreatedAtDesc(
            String status, Pageable pageable);

    Optional<EmailNotificationRow> findByCommandId(String commandId);

    List<EmailNotificationRow> findByOrderIdOrderByCreatedAtDesc(String orderId);

    @Query("""
        SELECT e.status, COUNT(e) FROM EmailNotificationRow e GROUP BY e.status
    """)
    List<Object[]> countByStatus();
}
