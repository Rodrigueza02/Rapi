package aggregator.service;

import aggregator.dto.DashboardStatsDto;
import aggregator.dto.DriverViewDto;
import aggregator.dto.RoundRobinDto;
import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;
import dtos.DriverAssignedEvent;
import dtos.OrderCancelledEvent;
import dtos.OrderEvent;
import dtos.OrderPreparedEvent;

import java.util.*;

public class DashboardAggregatorService {

    private final OrderViewRepositoryPort repository;
    private final DashboardBroadcastPort  broadcaster;

    private final LinkedHashSet<String> driverRing   = new LinkedHashSet<>();
    private int roundRobinIndex = 0;

    public DashboardAggregatorService(OrderViewRepositoryPort repository,
                                      DashboardBroadcastPort broadcaster) {
        this.repository  = Objects.requireNonNull(repository);
        this.broadcaster = Objects.requireNonNull(broadcaster);
    }

    public void process(OrderEvent event) {
        Objects.requireNonNull(event, "event");

        var existing = repository.findById(event.orderId());

        if (existing.isEmpty()) {
            OrderView newView;
            if (event instanceof OrderPreparedEvent prepared) {
                newView = OrderView.from(prepared);
            } else if (event instanceof DriverAssignedEvent assigned) {
                trackDriver(assigned.driverId());
                newView = OrderView.from(assigned);
            } else {
                throw new IllegalStateException("Primer evento debe ser ORDER_PREPARED o DRIVER_ASSIGNED");
            }
            repository.save(newView);
            broadcaster.broadcast(newView);
            return;
        }

        OrderView view = existing.get();
        if (event instanceof DriverAssignedEvent assigned)
            trackDriver(assigned.driverId());

        boolean changed = view.apply(event);
        if (!changed) return;

        repository.save(view);
        broadcaster.broadcast(view);
    }

    public List<OrderView> findAll()                      { return repository.findAll(); }
    public Optional<OrderView> findById(String orderId)   { return repository.findById(orderId); }

    /**
     * active = pedidos en estados no terminales (excluye DELIVERED y CANCELLED)
     */
    public DashboardStatsDto getStats() {
        List<OrderView> all = repository.findAll();

        long active = all.stream()
                .filter(o -> o.getState().isActive())
                .count();
        long delivered = all.stream()
                .filter(o -> o.getState() == OrderViewState.DELIVERED)
                .count();

        return new DashboardStatsDto(active, delivered, all.size(), driverRing.size());
    }

    public RoundRobinDto getRoundRobin() {
        List<String> drivers = new ArrayList<>(driverRing);
        if (drivers.isEmpty()) return new RoundRobinDto(drivers, 0, null, "0 / 0");
        int    idx   = roundRobinIndex % drivers.size();
        String next  = drivers.get(idx);
        return new RoundRobinDto(drivers, idx, next, (idx + 1) + " / " + drivers.size());
    }

    public List<DriverViewDto> getDrivers() {
        List<OrderView> all = repository.findAll();
        return new ArrayList<>(driverRing).stream().map(driverId -> {
            List<OrderView> mine = all.stream()
                    .filter(o -> driverId.equals(o.getDriverId())).toList();
            Optional<OrderView> active = mine.stream()
                    .filter(o -> o.getState().isActive()).findFirst();
            long completed = mine.stream()
                    .filter(o -> o.getState() == OrderViewState.DELIVERED).count();
            String lastActivity = mine.stream()
                    .max(Comparator.comparing(OrderView::getLastUpdated))
                    .map(o -> o.getLastUpdated().toString()).orElse(null);
            return new DriverViewDto(driverId,
                    active.isPresent() ? "EN_RUTA" : "DESCONOCIDO",
                    active.map(OrderView::getOrderId).orElse(null),
                    active.map(o -> o.getState().name()).orElse(null),
                    completed, mine.size(), lastActivity);
        }).toList();
    }

    private void trackDriver(String driverId) {
        driverRing.add(driverId);
        if (!driverRing.isEmpty())
            roundRobinIndex = (roundRobinIndex + 1) % driverRing.size();
    }
}
