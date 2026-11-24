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
 *
 * <p><strong>Sin dependencia circular:</strong> este adaptador no conoce
 * {@code DashboardAggregatorService}. El conteo de asignaciones históricas se
 * expone a través de {@link #countAssignedEvents()} y lo consume {@code BeanConfig}
 * en un {@code ApplicationListener<ContextRefreshedEvent>}, tras la inicialización
 * completa del contexto Spring.
 */
@Repository
public class OrderHistoryRepositoryAdapter implements OrderViewRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryRepositoryAdapter.class);

    private final OrderHistoryJpaRepository jpaRepository;

    /** Proyección en memoria: estado actual de cada pedido. */
    private final Map<String, OrderView> currentOrders = new ConcurrentHashMap<>();

    /** Próximo sequence_num por pedido. */
    private final Map<String, Integer>   nextSeqNum    = new ConcurrentHashMap<>();

    /** Número de eventos DRIVER_ASSIGNED encontrados en el último replay. */
    private volatile int assignedEventsCount = 0;

    public OrderHistoryRepositoryAdapter(OrderHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Reconstruye la proyección en memoria aplicando los eventos persistidos en
     * orden de {@code sequence_num}. Llamado automáticamente por Spring al arrancar.
     */
    @PostConstruct
    public void loadHistory() {
        currentOrders.clear();
        nextSeqNum.clear();
        assignedEventsCount = 0;

        List<OrderHistoryEntity> allEvents =
                jpaRepository.findAllByOrderByOrderIdAscSequenceNumAscOccurredAtAsc();

        for (OrderHistoryEntity entity : allEvents) {
            String orderId = entity.getOrderId();

            // Primer evento del pedido: establecer la vista inicial
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

            // Eventos siguientes: actualizar al estado más reciente, preservando
            // los campos enriquecidos del primer evento (address, items, total)
            if (entity.getSequenceNum() > 0) {
                currentOrders.put(orderId, OrderView.restoreFull(
                        entity.getOrderId(),
                        entity.getCustomerId()      != null ? entity.getCustomerId()      : view.getCustomerId(),
                        entity.getDeliveryAddress() != null ? entity.getDeliveryAddress() : view.getDeliveryAddress(),
                        entity.getTotalAmount()     != null ? entity.getTotalAmount()     : view.getTotalAmount(),
                        entity.getItemsJson()       != null ? entity.getItemsJson()       : view.getItemsJson(),
                        entity.getDriverId() != null && !"unknown".equals(entity.getDriverId())
                                ? entity.getDriverId() : view.getDriverId(),
                        entity.getState(),
                        entity.getOccurredAt()
                ));
            }

            if (dtos.Names.EVENT_DRIVER_ASSIGNED.equals(entity.getEventType())) {
                assignedEventsCount++;
            }

            nextSeqNum.merge(orderId, entity.getSequenceNum() + 1, Math::max);
        }

        log.info("Event Store replay completado: {} pedidos reconstruidos desde {} eventos ({} asignaciones)",
                currentOrders.size(), allEvents.size(), assignedEventsCount);
    }

    /**
     * Número de eventos {@code DRIVER_ASSIGNED} encontrados durante el último replay.
     * Consumido por {@code BeanConfig} para inicializar el índice Round Robin del
     * aggregator una vez que todos los beans de Spring están listos.
     */
    public int countAssignedEvents() {
        return assignedEventsCount;
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
     */
    @Override
    public void save(OrderView view, String eventType) {
        String orderId = view.getOrderId();
        int seq = nextSeqNum.getOrDefault(orderId, 0);

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

    public List<OrderHistoryEntity> findEventsByOrderId(String orderId) {
        return jpaRepository.findByOrderIdOrderBySequenceNumAscOccurredAtAsc(orderId);
    }
}
