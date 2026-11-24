package emailworker.infrastructure.persistence;

import emailworker.domain.EmailStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailNotificationJpaRepository
        extends JpaRepository<EmailNotificationEntity, UUID> {

    @Query("SELECT e FROM EmailNotificationEntity e WHERE e.content.commandId = :commandId")
    Optional<EmailNotificationEntity> findByCommandId(@Param("commandId") String commandId);

    @Transactional
    @Modifying
    @Query("""
        UPDATE EmailNotificationEntity e
        SET e.status = :toStatus, e.updatedAt = :now
        WHERE e.content.commandId = :commandId AND e.status = :fromStatus
    """)
    int conditionalUpdateStatus(
            @Param("commandId")  String commandId,
            @Param("fromStatus") EmailStatus fromStatus,
            @Param("toStatus")   EmailStatus toStatus,
            @Param("now")        Instant now
    );

    @Transactional
    @Modifying
    @Query("""
        UPDATE EmailNotificationEntity e
        SET e.status = :uncertain, e.errorMessage = :reason, e.updatedAt = :now
        WHERE e.status = :sending AND e.updatedAt < :threshold
    """)
    int markStuckSendingAsUncertain(
            @Param("threshold") Instant threshold,
            @Param("reason")    String reason,
            @Param("now")       Instant now,
            @Param("sending")   EmailStatus sending,
            @Param("uncertain") EmailStatus uncertain
    );
}
