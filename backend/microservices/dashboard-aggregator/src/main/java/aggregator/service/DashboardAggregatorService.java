
package aggregator.service;

import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;

import dtos.DriverAssignedEvent;
import dtos.OrderEvent;

import java.util.List;
import java.util.Objects;

public class DashboardAggregatorService {

    private final OrderViewRepositoryPort repository;
    private final DashboardBroadcastPort broadcaster;

    public DashboardAggregatorService(
            OrderViewRepositoryPort repository,
            DashboardBroadcastPort broadcaster) {

        this.repository = Objects.requireNonNull(repository);
        this.broadcaster = Objects.requireNonNull(broadcaster);
    }

    /**
     * Procesa los eventos que recibe el Dashboard.
     */
    public void process(OrderEvent event) {
        Objects.requireNonNull(event, "event");

        var existing = repository.findById(event.orderId());

        if (existing.isEmpty()) {

            if (!(event instanceof DriverAssignedEvent assigned)) {
                throw new IllegalStateException(
                        "El pedido debe iniciar con DRIVER_ASSIGNED"
                );
            }

            OrderView newView = OrderView.from(assigned);

            repository.save(newView);
            broadcaster.broadcast(newView);
            return;
        }

        OrderView currentView = existing.get();

        boolean changed = currentView.apply(event);

        if (!changed) {
            // No repetir persistencia ni notificaciones.
            return;
        }

        repository.save(currentView);
        broadcaster.broadcast(currentView);
    }

    
    public List<OrderView> findAll() {
        return repository.findAll();
    }
}
