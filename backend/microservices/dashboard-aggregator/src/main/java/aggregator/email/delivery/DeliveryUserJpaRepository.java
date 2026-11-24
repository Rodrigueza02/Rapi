package aggregator.email.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryUserJpaRepository
        extends JpaRepository<DeliveryUserRow, UUID> {
}
