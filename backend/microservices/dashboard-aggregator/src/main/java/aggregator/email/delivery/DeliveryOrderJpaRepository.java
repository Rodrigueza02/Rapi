package aggregator.email.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryOrderJpaRepository
        extends JpaRepository<DeliveryOrderRow, String> {
}
