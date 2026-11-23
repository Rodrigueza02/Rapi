package aggregator.service;

import aggregator.dto.DashboardStatsDto;
import aggregator.dto.RoundRobinDto;
import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;
import dtos.DriverAssignedEvent;
import dtos.OrderEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DashboardAggregatorService {

    private final OrderViewRepositoryPort repository;
    private final DashboardBroadcastPort  broadcaster;

    /**
     * Lista ordenada de repartidores vistos (orden de primera aparición).
     * Sirve como anillo Round Robin — el backend ya decidió la asignación;
     * aquí solo llevamos el índice para mostrárselo al frontend.
     */
    private final LinkedHashSet<String> driverRing = new LinkedHashSet<>();
    private int roundRobinIndex = 0;

    public DashboardAggregatorService(
            OrderViewRepositoryPort repository,
            DashboardBroadcastPort  broadcaster) {
        this.repository  = Objects.requireNonNull(repository);
        this.broadcaster = Objects.requireNonNull(broadcaster);
    }

    // ── Procesamiento de eventos Kafka ────────────────────────────────────────

    public void process(OrderEvent event) {
        Objects.requireNonNull(event, "event");

        var existing = repository.findById(event.orderId());

        if (existing.isEmpty()) {
            if (!(event instanceof DriverAssignedEvent assigned)) {
                throw new IllegalStateException(
                        "El pedido debe iniciar con DRIVER_ASSIGNED"
                );
            }
            // Registrar el repartidor en el anillo
            trackDriver(assigned.driverId());

            OrderView newView = OrderView.from(assigned);
            repository.save(newView);
            broadcaster.broadcast(newView);
            return;
        }

        OrderView currentView = existing.get();
        boolean changed = currentView.apply(event);
        if (!changed) return;

        repository.save(currentView);
        broadcaster.broadcast(currentView);
    }

    // ── Consultas para la API REST ────────────────────────────────────────────

    public List<OrderView> findAll() {
        return repository.findAll();
    }

    public Optional<OrderView> findById(String orderId) {
        return repository.findById(orderId);
    }

    /**
     * Calcula los contadores que muestran las 4 tarjetas superiores.
     * active  = ASSIGNED + ARRIVING
     * delivered = DELIVERED
     * total     = todos
     * drivers   = repartidores únicos vistos
     */
    public DashboardStatsDto getStats() {
        List<OrderView> all = repository.findAll();

        long active = all.stream()
                .filter(o -> o.getState() == OrderViewState.ASSIGNED
                          || o.getState() == OrderViewState.ARRIVING)
                .count();

        long delivered = all.stream()
                .filter(o -> o.getState() == OrderViewState.DELIVERED)
                .count();

        return new DashboardStatsDto(
                active,
                delivered,
                all.size(),
                driverRing.size()
        );
    }

    /**
     * Estado actual del anillo Round Robin.
     * El frontend solo visualiza lo que el backend ya decidió.
     */
    public RoundRobinDto getRoundRobin() {
        List<String> drivers = new ArrayList<>(driverRing);

        if (drivers.isEmpty()) {
            return new RoundRobinDto(drivers, 0, null, "0 / 0");
        }

        int    idx        = roundRobinIndex % drivers.size();
        String nextDriver = drivers.get(idx);
        String turnLabel  = (idx + 1) + " / " + drivers.size();

        return new RoundRobinDto(drivers, idx, nextDriver, turnLabel);
    }

    // ── Privado ───────────────────────────────────────────────────────────────

    /**
     * Añade el repartidor al anillo y avanza el índice cada vez que
     * se le asigna un pedido nuevo — refleja lo que hizo el backend.
     */
    private void trackDriver(String driverId) {
        driverRing.add(driverId);
        if (!driverRing.isEmpty()) {
            roundRobinIndex = (roundRobinIndex + 1) % driverRing.size();
        }
    }
}
