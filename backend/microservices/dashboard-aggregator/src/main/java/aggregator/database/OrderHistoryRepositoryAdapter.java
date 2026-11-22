
package aggregator.database;

import aggregator.model.OrderView;
import aggregator.ports.OrderViewRepositoryPort;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class OrderHistoryRepositoryAdapter
        implements OrderViewRepositoryPort {

    private final OrderHistoryJpaRepository jpaRepository;

    private final Map<String, OrderView> currentOrders =
            new ConcurrentHashMap<>();

    public OrderHistoryRepositoryAdapter(
            OrderHistoryJpaRepository jpaRepository) {

        this.jpaRepository = jpaRepository;
    }

    /**
     * Recupera las últimas vistas guardadas
     * cuando se inicia el microservicio.
     */
    @PostConstruct
    public void loadHistory() {

        currentOrders.clear();

        List<OrderHistoryEntity> history =
                jpaRepository.findAllByOrderByOccurredAtAsc();

        for (OrderHistoryEntity entity : history) {

            OrderView view = OrderView.restore(
                    entity.getOrderId(),
                    entity.getDriverId(),
                    entity.getState(),
                    entity.getOccurredAt()
            );

            currentOrders.put(entity.getOrderId(), view);
        }
    }

    /**
     * Busca la vista actual de un pedido.
     */
    @Override
    public Optional<OrderView> findById(String orderId) {

        OrderView view = currentOrders.get(orderId);

        if (view == null) {
            return Optional.empty();
        }

        return Optional.of(copy(view));
    }

    /**
     * Guarda una transición en PostgreSQL
     * y actualiza la vista en memoria.
     */
    @Override
    public void save(OrderView orderView) {

        OrderHistoryEntity history =
                OrderHistoryEntity.from(orderView);

        // Primero persiste el historial.
        jpaRepository.saveAndFlush(history);

        // Después actualiza la vista en memoria.
        currentOrders.put(
                orderView.getOrderId(),
                copy(orderView)
        );
    }

    /**
     * Devuelve las vistas actuales de los pedidos.
     */
    @Override
    public List<OrderView> findAll() {

        List<OrderView> result = new ArrayList<>();

        for (OrderView view : currentOrders.values()) {
            result.add(copy(view));
        }

        return result;
    }

    /**
     * Evita compartir objetos modificables
     * directamente desde la memoria interna.
     */
    private OrderView copy(OrderView view) {

        return OrderView.restore(
                view.getOrderId(),
                view.getDriverId(),
                view.getState(),
                view.getLastUpdated()
        );
    }
}
