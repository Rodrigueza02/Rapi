package aggregator.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderHistoryJpaRepository
        extends JpaRepository<OrderHistoryEntity, UUID> {

    /** Todos los eventos de un pedido, en orden cronológico. Para replay. */
    List<OrderHistoryEntity> findByOrderIdOrderBySequenceNumAscOccurredAtAsc(String orderId);

    /** Todos los eventos de todos los pedidos, ordenados para reconstrucción al arrancar. */
    List<OrderHistoryEntity> findAllByOrderByOrderIdAscSequenceNumAscOccurredAtAsc();

    /** Último evento registrado de un pedido (para calcular el siguiente sequence_num). */
    Optional<OrderHistoryEntity> findTopByOrderIdOrderBySequenceNumDesc(String orderId);

    /** Estado actual de cada pedido: último evento por order_id. */
    @Query("""
        SELECT e FROM OrderHistoryEntity e
        WHERE e.sequenceNum = (
            SELECT MAX(e2.sequenceNum)
            FROM OrderHistoryEntity e2
            WHERE e2.orderId = e.orderId
        )
        ORDER BY e.orderId
        """)
    List<OrderHistoryEntity> findCurrentStatePerOrder();
}
