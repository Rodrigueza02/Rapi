
package aggregator.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryJpaRepository
        extends JpaRepository<OrderHistoryEntity, UUID> {

    List<OrderHistoryEntity>
        findByOrderIdOrderByOccurredAtAsc(String orderId);

    List<OrderHistoryEntity>
        findAllByOrderByOccurredAtAsc();
}
