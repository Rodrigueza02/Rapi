package aggregator.database;

import aggregator.model.OrderView;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de Event Store para el dashboard.
 *
 * <p>Principio de Event Sourcing:
 * <ul>
 *   <li>Cada llamada a {@link #save(OrderView, String)} inserta una fila nueva e inmutable
 *       (append-only). Nunca actualiza filas existentes.</li>
 *   <li>Al arrancar ({@link #loadHistory()}), reconstruye el estado de cada pedido
 *       aplicando todos sus eventos en orden de {@code sequence_num}.</li>
 *   <li>La proyección en memoria ({@code currentOrders}) siempre refleja el estado
 *       actual de cada pedido como resultado del replay.</li>
 * </ul>
 *
 * <p>Idempotencia: si ya existe un evento con el mismo {@code sequence_num}
 * para el mismo pedido, se omite la inserción.
 */
@Repository
public class OrderHistoryRepositoryAdapter implements OrderViewRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryRepositoryAdapter.class);

    private final OrderHistoryJpaRepository jpaRepository;

    /** Proyección en memoria: estado actual de cada pedido. */
    private final Map<String, OrderView>    currentOrders = new ConcurrentHashMap<>();

    /** Próximo sequence_num por pedido. */
    private final Map<String, Integer>      nextSeqNum    = new ConcurrentHashMap<>();

    public OrderHistoryRepositoryAdapter(OrderHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Reconstruye la proyección en memoria aplicando los eventos persistidos en
     * orden de sequence_num. Llamado al arrancar el servicio.
     */
    @PostConstruct
    public void loadHistory() {
        currentOrders.clear();
        nextSeqNum.clear();

        List<OrderHistoryEntity> allEvents =
                jpaRepository.findAllByOrderByOrderIdAscSequenceNumAscOccurredAtAsc();

        for (OrderHistoryEntity entity : allEvents) {
            String orderId = entity.getOrderId();

            // Reconstruir o actualizar la proyección
            OrderView view = currentOrders.computeIfAbsent(orderId, id ->
                    OrderView.restoreFull(
                            entity.getOrderId(),
                            entity.getCustomerId(),
                            entity.getDeliveryAddress(),
                            entity.getTotalAmount(),
                            entity.getItemsJson(),
                            entity.getDriverId(),
                            entity.getState(),
                            entity.getOccurredAt()
                    )
            );

            // Para registros que no son el primero, actualizar el estado
            if (currentOrders.containsKey(orderId) &&
                    entity.getSequenceNum() > 0) {
                // Actualizar la vista al estado más reciente de la secuencia
                currentOrders.put(orderId, OrderView.restoreFull(
                        entity.getOrderId(),
                        entity.getCustomerId() != null ? entity.getCustomerId() : view.getCustomerId(),
                        entity.getDeliveryAddress() != null ? entity.getDeliveryAddress() : view.getDeliveryAddress(),
                        entity.getTotalAmount() != null ? entity.getTotalAmount() : view.getTotalAmount(),
                        entity.getItemsJson() != null ? entity.getItemsJson() : view.getItemsJson(),
                        entity.getDriverId() != null && !entity.getDriverId().equals("unknown")
                                ? entity.getDriverId() : view.getDriverId(),
                        entity.getState(),
                        entity.getOccurredAt()
                ));
            }

            // Actualizar el próximo sequence_num
            nextSeqNum.merge(orderId, entity.getSequenceNum() + 1, Math::max);
        }

        log.info("Event Store replay completado: {} pedidos reconstruidos desde {} eventos",
                currentOrders.size(), allEvents.size());
    }

    @Override
    public Optional<OrderView> findById(String id) {
        return Optional.ofNullable(currentOrders.get(id));
    }

    @Override
    public List<OrderView> findAll() {
        return new ArrayList<>(currentOrders.values());
    }

    /**
     * Persiste el nuevo estado del pedido como un evento inmutable y actualiza
     * la proyección en memoria.
     *
     * @param view      proyección actualizada del pedido
     * @param eventType tipo canónico del evento (ej. "ORDER_CREATED")
     */
    @Override
    public void save(OrderView view, String eventType) {
        String orderId = view.getOrderId();
        int seq = nextSeqNum.getOrDefault(orderId, 0);

        // Idempotencia: verificar si ya existe un evento igual antes de insertar
        boolean alreadyExists = jpaRepository
                .findTopByOrderIdOrderBySequenceNumDesc(orderId)
                .map(last -> last.getSequenceNum() == seq - 1
                        && last.getState() == view.getState()
                        && last.getOccurredAt().equals(view.getLastUpdated()))
                .orElse(false);

        if (!alreadyExists) {
            OrderHistoryEntity entity = OrderHistoryEntity.from(view, eventType, seq);
            jpaRepository.saveAndFlush(entity);
            nextSeqNum.put(orderId, seq + 1);
            log.debug("Evento persistido: orderId={} eventType={} seq={} state={}",
                    orderId, eventType, seq, view.getState());
        } else {
            log.debug("Evento duplicado ignorado: orderId={} state={}", orderId, view.getState());
        }

        currentOrders.put(orderId, view);
    }

    /**
     * Devuelve todos los eventos de un pedido en orden de secuencia.
     * Permite reconstruir el estado completo desde cero (replay).
     */
    public List<OrderHistoryEntity> findEventsByOrderId(String orderId) {
        return jpaRepository.findByOrderIdOrderBySequenceNumAscOccurredAtAsc(orderId);
    }
}
