
package aggregator.service;

import aggregator.dto.DriverViewDto;
import aggregator.model.OrderView;
import aggregator.ports.DashboardBroadcastPort;
import aggregator.ports.OrderViewRepositoryPort;
import aggregator.state.OrderViewState;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardAggregatorServiceTest {

    private OrderViewRepositoryPort repository;
    private DashboardBroadcastPort broadcaster;
    private DashboardAggregatorService service;

    private final Instant start =
            Instant.parse("2026-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        repository = mock(OrderViewRepositoryPort.class);
        broadcaster = mock(DashboardBroadcastPort.class);

        service = new DashboardAggregatorService(
                repository,
                broadcaster
        );
    }

    @Test
    void shouldCreateOrderWhenDriverIsAssigned() {

        when(repository.findById("order-1"))
                .thenReturn(Optional.empty());

        DriverAssignedEvent event =
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start
                );

        service.process(event);

        ArgumentCaptor<OrderView> captor =
                ArgumentCaptor.forClass(OrderView.class);

        verify(repository).save(captor.capture());

        OrderView saved = captor.getValue();

        assertEquals("order-1", saved.getOrderId());
        assertEquals(OrderViewState.ASSIGNED, saved.getState());

        verify(broadcaster).broadcast(saved);
    }

    @Test
    void shouldUpdateOrderWhenDriverIsArriving() {

        OrderView view = createAssignedOrder();

        when(repository.findById("order-1"))
                .thenReturn(Optional.of(view));

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60)
                );

        service.process(event);

        assertEquals(OrderViewState.ARRIVING, view.getState());

        verify(repository).save(view);
        verify(broadcaster).broadcast(view);
    }

    @Test
    void shouldMarkOrderAsDelivered() {

        OrderView view = createAssignedOrder();

        view.apply(new DriverArrivingEvent(
                "order-1",
                "driver-1", "customer-1",
                start.plusSeconds(60)
        ));

        when(repository.findById("order-1"))
                .thenReturn(Optional.of(view));

        service.process(new OrderDeliveredEvent(
                "order-1",
                "driver-1", "customer-1",
                start.plusSeconds(120)
        ));

        assertEquals(OrderViewState.DELIVERED, view.getState());

        verify(repository).save(view);
        verify(broadcaster).broadcast(view);
    }

    @Test
    void shouldNotRepeatOperationsForDuplicateEvent() {

        OrderView view = createAssignedOrder();

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60)
                );

        view.apply(event);

        when(repository.findById("order-1"))
                .thenReturn(Optional.of(view));

        service.process(event);

        verify(repository, never()).save(any());
        verify(broadcaster, never()).broadcast(any());
    }

    @Test
    void shouldRejectArrivingEventWithoutAssignedOrder() {

        when(repository.findById("order-1"))
                .thenReturn(Optional.empty());

        DriverArrivingEvent event =
                new DriverArrivingEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start.plusSeconds(60)
                );

        assertThrows(
                IllegalStateException.class,
                () -> service.process(event)
        );

        verify(repository, never()).save(any());
        verify(broadcaster, never()).broadcast(any());
    }

    @Test
    void shouldReturnAllOrders() {

        OrderView view = createAssignedOrder();

        when(repository.findAll())
                .thenReturn(List.of(view));

        List<OrderView> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("order-1", result.get(0).getOrderId());
    }

    private OrderView createAssignedOrder() {
        return OrderView.from(
                new DriverAssignedEvent(
                        "order-1",
                        "driver-1", "customer-1",
                        start
                )
        );
    }

    // ── Tests para getDrivers() ───────────────────────────────────────────────

    /**
     * Cuando no hay pedidos en memoria (driverRing vacío), getDrivers() devuelve lista vacía.
     * No inventa repartidores.
     */
    @Test
    void getDriversShouldReturnEmptyWhenNoOrdersReceived() {
        when(repository.findAll()).thenReturn(List.of());

        List<DriverViewDto> result = service.getDrivers();

        assertTrue(result.isEmpty(),
                "Sin pedidos procesados, la lista de repartidores debe estar vacía");
    }

    /**
     * Un repartidor con pedido en ASSIGNED tiene status EN_RUTA.
     * El status no se inventa; se deriva del pedido activo.
     */
    @Test
    void getDriversShouldMarkDriverEnRutaWhenActiveOrderExists() {
        // Primero procesar el evento para que driver-1 entre al driverRing
        when(repository.findById("order-1")).thenReturn(Optional.empty());
        DriverAssignedEvent assignedEvent = new DriverAssignedEvent(
                "order-1", "driver-1", "customer-1", start);
        service.process(assignedEvent);

        // Ahora simular que repository devuelve ese pedido en ASSIGNED
        OrderView view = OrderView.from(assignedEvent);
        when(repository.findAll()).thenReturn(List.of(view));

        List<DriverViewDto> result = service.getDrivers();

        assertEquals(1, result.size());
        DriverViewDto dto = result.get(0);
        assertEquals("driver-1", dto.driverId());
        assertEquals("EN_RUTA",  dto.status());
        assertEquals("order-1",  dto.activeOrderId());
        assertEquals("ASSIGNED", dto.activeOrderState());
        assertEquals(0,          dto.completedOrders());
        assertEquals(1,          dto.totalOrders());
        assertNotNull(dto.lastActivity());
    }

    /**
     * Un repartidor sin pedidos activos (solo DELIVERED) tiene status DESCONOCIDO.
     * No se le marca "Disponible" ni "Desconectado" — no existe ese dato en el backend.
     */
    @Test
    void getDriversShouldMarkDriverDesconocidoWhenAllOrdersDelivered() {
        // Registrar driver-2 en el anillo procesando un evento
        when(repository.findById("order-2")).thenReturn(Optional.empty());
        DriverAssignedEvent assignedEvent = new DriverAssignedEvent(
                "order-2", "driver-2", "customer-2", start);
        service.process(assignedEvent);

        // Simular pedido ya DELIVERED
        OrderView delivered = OrderView.from(assignedEvent);
        delivered.apply(new DriverArrivingEvent("order-2", "driver-2", "customer-2", start.plusSeconds(10)));
        delivered.apply(new OrderDeliveredEvent("order-2", "driver-2", "customer-2", start.plusSeconds(20)));
        when(repository.findAll()).thenReturn(List.of(delivered));

        List<DriverViewDto> result = service.getDrivers();

        assertEquals(1, result.size());
        DriverViewDto dto = result.get(0);
        assertEquals("driver-2",    dto.driverId());
        assertEquals("DESCONOCIDO", dto.status(),
                "Sin pedido activo no podemos saber si está disponible o desconectado");
        assertNull(dto.activeOrderId());
        assertNull(dto.activeOrderState());
        assertEquals(1, dto.completedOrders());
        assertEquals(1, dto.totalOrders());
    }

    /**
     * getDrivers() NO modifica driverRing ni roundRobinIndex.
     * El Round Robin permanece intacto tras consultar repartidores.
     */
    @Test
    void getDriversShouldNotModifyRoundRobinState() {
        when(repository.findById("order-3")).thenReturn(Optional.empty());
        service.process(new DriverAssignedEvent("order-3", "driver-3", "customer-3", start));

        when(repository.findById("order-4")).thenReturn(Optional.empty());
        service.process(new DriverAssignedEvent("order-4", "driver-4", "customer-4", start.plusSeconds(1)));

        // Capturar estado del Round Robin antes de llamar getDrivers()
        var rrBefore = service.getRoundRobin();
        int indexBefore = rrBefore.currentIndex();
        String nextBefore = rrBefore.nextDriverId();

        // Llamar getDrivers() múltiples veces
        when(repository.findAll()).thenReturn(List.of());
        service.getDrivers();
        service.getDrivers();

        // El Round Robin debe ser idéntico
        var rrAfter = service.getRoundRobin();
        assertEquals(indexBefore, rrAfter.currentIndex(),
                "getDrivers() no debe modificar el índice Round Robin");
        assertEquals(nextBefore, rrAfter.nextDriverId(),
                "getDrivers() no debe modificar el próximo repartidor en turno");
    }

    /**
     * getDrivers() solo muestra repartidores que hayan pasado por driverRing.
     * No inventa repartidores a partir de configuración estática ni pedidos de otras fuentes.
     */
    @Test
    void getDriversShouldOnlyShowKnownDriversFromRing() {
        // Solo driver-1 ha procesado eventos; driver-2 nunca ha aparecido
        when(repository.findById("order-5")).thenReturn(Optional.empty());
        service.process(new DriverAssignedEvent("order-5", "driver-1", "customer-5", start));

        OrderView view = OrderView.from(
                new DriverAssignedEvent("order-5", "driver-1", "customer-5", start));
        // Intentar que repository devuelva también un pedido de driver-99 (no en ring)
        OrderView alien = OrderView.restore("order-alien", "driver-99",
                aggregator.state.OrderViewState.ASSIGNED, start);
        when(repository.findAll()).thenReturn(List.of(view, alien));

        List<DriverViewDto> result = service.getDrivers();

        // Solo debe aparecer driver-1; driver-99 y driver-2 no están en el anillo
        assertTrue(result.stream().allMatch(d -> d.driverId().equals("driver-1")),
                "Solo deben aparecer repartidores del driverRing, no repartidores externos");
        assertEquals(1, result.size());
    }
}

