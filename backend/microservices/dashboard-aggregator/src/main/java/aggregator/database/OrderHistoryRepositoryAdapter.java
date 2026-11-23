package aggregator.database;

import aggregator.model.OrderView;
import aggregator.ports.OrderViewRepositoryPort;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderHistoryRepositoryAdapter implements OrderViewRepositoryPort {

    private final OrderHistoryJpaRepository jpaRepository;
    private final Map<String, OrderView>    currentOrders = new ConcurrentHashMap<>();

    public OrderHistoryRepositoryAdapter(OrderHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @PostConstruct
    public void loadHistory() {
        currentOrders.clear();
        for (OrderHistoryEntity e : jpaRepository.findAllByOrderByOccurredAtAsc()) {
            OrderView view = OrderView.restoreFull(
                    e.getOrderId(), e.getCustomerId(), e.getDeliveryAddress(),
                    e.getTotalAmount(), e.getItemsJson(),
                    e.getDriverId(), e.getState(), e.getOccurredAt());
            currentOrders.put(e.getOrderId(), view);
        }
    }

    @Override public Optional<OrderView> findById(String id)   { return Optional.ofNullable(currentOrders.get(id)); }
    @Override public List<OrderView>     findAll()              { return new ArrayList<>(currentOrders.values()); }

    @Override
    public void save(OrderView v) {
        jpaRepository.saveAndFlush(OrderHistoryEntity.from(v));
        currentOrders.put(v.getOrderId(), v);
    }
}
