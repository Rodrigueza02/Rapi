
package aggregator.ports;

import aggregator.model.OrderView;
import java.util.List;
import java.util.Optional;


public interface OrderViewRepositoryPort {

    Optional<OrderView> findById(String orderId);

    void save(OrderView orderView);

    List<OrderView> findAll();
}
