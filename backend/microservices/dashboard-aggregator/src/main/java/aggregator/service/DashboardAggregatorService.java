package aggregator.service;

import aggregator.dto.DashboardStatsDto;
import aggregator.dto.DriverViewDto;
import aggregator.dto.RoundRobinDto;
import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;
import dtos.*;

import java.util.*;

/**
 * Servicio de proyección del dashboard.
 *
 * <p>Recibe eventos de dominio desde Kafka, actualiza la proyección en memoria
 * y delega al repositorio la persistencia append-only en el Event Store.
 * Cada evento se persiste con su {@code event_type} canónico.
 */
public class DashboardAggregatorService {

    private final OrderViewRepositoryPort repository;
    private final DashboardBroadcastPort  broadcaster;

    private final LinkedHashSet<String> driverRing  = new LinkedHashSet<>();
    private int roundRobinIndex = 0;

    public DashboardAggregatorService(OrderViewRepositoryPort repository,
                                      DashboardBroadcastPort broadcaster) {
        this.repository  = Objects.requireNonNull(repository,  "repository");
        this.broadcaster = Objects.requireNonNull(broadcaster, "broadcaster");
    }

    /**
     * Procesa un evento de dominio: actualiza la proyección y la persiste.
     * Idempotente: eventos duplicados (mismo estado + mismo timestamp) se ignoran.
     */
    public void process(OrderEvent event) {
        Objects.requireNonNull(event, "event");

        String eventType = resolveEventType(event);
        var existing = repository.findById(event.orderId());

        if (existing.isEmpty()) {
            // Primer evento del pedido: crear la vista inicial
            OrderView newView = createInitialView(event);
            if (newView == null) {
                throw new IllegalStateException(
                    "El primer evento de un pedido debe ser ORDER_CREATED, " +
                    "ORDER_PREPARED o DRIVER_ASSIGNED, recibido: " + eventType);
            }
            if (event instanceof DriverAssignedEvent assigned) {
                trackDriver(assigned.driverId());
            }
            repository.save(newView, eventType);
            broadcaster.broadcast(newView);
            return;
        }

        OrderView view = existing.get();
        if (event instanceof DriverAssignedEvent assigned) {
            trackDriver(assigned.driverId());
        }

        boolean changed = view.apply(event);
        if (!changed) return;

        repository.save(view, eventType);
        broadcaster.broadcast(view);
    }

    public List<OrderView> findAll()                    { return repository.findAll(); }
    public Optional<OrderView> findById(String orderId) { return repository.findById(orderId); }

    public DashboardStatsDto getStats() {
        List<OrderView> all = repository.findAll();
        long active    = all.stream().filter(o -> o.getState().isActive()).count();
        long delivered = all.stream().filter(o -> o.getState() == OrderViewState.DELIVERED).count();
        return new DashboardStatsDto(active, delivered, all.size(), driverRing.size());
    }

    public RoundRobinDto getRoundRobin() {
        List<String> drivers = new ArrayList<>(driverRing);
        if (drivers.isEmpty()) return new RoundRobinDto(drivers, 0, null, "0 / 0");
        int    idx  = roundRobinIndex % drivers.size();
        String next = drivers.get(idx);
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

    // ── privado ───────────────────────────────────────────────────────────────

    private OrderView createInitialView(OrderEvent event) {
        if (event instanceof OrderCreatedEvent created)   return OrderView.from(created);
        if (event instanceof OrderPreparedEvent prepared) return OrderView.from(prepared);
        if (event instanceof DriverAssignedEvent assigned) return OrderView.from(assigned);
        return null;
    }

    /** Resuelve el nombre canónico del evento usando la misma clave que Jackson. */
    private static String resolveEventType(OrderEvent event) {
        if (event instanceof OrderCreatedEvent)   return dtos.Names.EVENT_ORDER_CREATED;
        if (event instanceof OrderPreparedEvent)  return dtos.Names.EVENT_ORDER_PREPARED;
        if (event instanceof DriverAssignedEvent) return dtos.Names.EVENT_DRIVER_ASSIGNED;
        if (event instanceof OrderOnTheWayEvent)  return dtos.Names.EVENT_ORDER_ON_THE_WAY;
        if (event instanceof DriverArrivingEvent) return dtos.Names.EVENT_DRIVER_ARRIVING;
        if (event instanceof OrderDeliveredEvent) return dtos.Names.EVENT_ORDER_DELIVERED;
        if (event instanceof OrderCancelledEvent) return dtos.Names.EVENT_ORDER_CANCELLED;
        return event.getClass().getSimpleName().toUpperCase();
    }

    private void trackDriver(String driverId) {
        driverRing.add(driverId);
        if (!driverRing.isEmpty())
            roundRobinIndex = (roundRobinIndex + 1) % driverRing.size();
    }
}
