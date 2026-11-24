package aggregator.ports;

import aggregator.model.OrderView;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar las proyecciones del dashboard.
 *
 * <p>El método {@link #save(OrderView, String)} recibe el tipo de evento que
 * originó el cambio de estado, permitiendo que el adaptador lo almacene en
 * el Event Store de forma append-only.
 */
public interface OrderViewRepositoryPort {

    Optional<OrderView> findById(String orderId);

    /**
     * Persiste el nuevo estado del pedido como un evento inmutable.
     *
     * @param view      proyección actualizada
     * @param eventType tipo canónico del evento (ej. "ORDER_CREATED")
     */
    void save(OrderView view, String eventType);

    List<OrderView> findAll();
}
