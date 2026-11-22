
package aggregator.model;

import aggregator.state.OrderViewState;

import dtos.DriverArrivingEvent;
import dtos.DriverAssignedEvent;
import dtos.OrderDeliveredEvent;
import dtos.OrderEvent;

import java.time.Instant;
import java.util.Objects;


public class OrderView {

    private final String orderId;
    private final String driverId;

    private OrderViewState state;
    private Instant lastUpdated;

    private OrderView(
            String orderId,
            String driverId,
            OrderViewState state,
            Instant lastUpdated) {

        this.orderId = requireText(orderId, "orderId");
        this.driverId = requireText(driverId, "driverId");
        this.state = Objects.requireNonNull(state);
        this.lastUpdated = Objects.requireNonNull(lastUpdated);
    }

    /**
     * Crea un pedido cuando se asigna el repartidor.
     */
    public static OrderView from(DriverAssignedEvent event) {
        Objects.requireNonNull(event);

        return new OrderView(
                event.orderId(),
                event.driverId(),
                OrderViewState.ASSIGNED,
                event.occurredAt()
        );
    }

    /**
     * Reconstruye una vista guardada anteriormente.
     * Se utilizará al recuperar información de PostgreSQL.
     */
    public static OrderView restore(
            String orderId,
            String driverId,
            OrderViewState state,
            Instant lastUpdated) {

        return new OrderView(
                orderId,
                driverId,
                state,
                lastUpdated
        );
    }

    /**
     * Aplica un evento.
     *
     * true: el pedido cambió de estado.
     * false: el evento ya estaba aplicado.
     */
    public boolean apply(OrderEvent event) {
        Objects.requireNonNull(event);

        if (!orderId.equals(event.orderId())) {
            throw new IllegalArgumentException(
                    "El evento pertenece a otro pedido"
            );
        }

        if (!driverId.equals(event.driverId())) {
            throw new IllegalArgumentException(
                    "El repartidor no coincide"
            );
        }

        OrderViewState nextState = stateFrom(event);

        // Evita aplicar dos veces el mismo evento.
        if (state == nextState) {
            if (lastUpdated.equals(event.occurredAt())) {
                return false;
            }

            throw new IllegalStateException(
                    "Evento repetido con una fecha diferente"
            );
        }

        if (!state.canTransitionTo(nextState)) {
            throw new IllegalStateException(
                    "Transición inválida: "
                    + state + " -> " + nextState
            );
        }

        if (event.occurredAt().isBefore(lastUpdated)) {
            throw new IllegalArgumentException(
                    "La fecha del evento es anterior"
            );
        }

        this.state = nextState;
        this.lastUpdated = event.occurredAt();

        return true;
    }

    /**
     * Traduce un evento Kafka a un estado del dominio.
     */
    private static OrderViewState stateFrom(OrderEvent event) {

        if (event instanceof DriverAssignedEvent) {
            return OrderViewState.ASSIGNED;
        }

        if (event instanceof DriverArrivingEvent) {
            return OrderViewState.ARRIVING;
        }

        if (event instanceof OrderDeliveredEvent) {
            return OrderViewState.DELIVERED;
        }

        throw new IllegalArgumentException(
                "Tipo de evento no soportado"
        );
    }

    private static String requireText(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " no puede estar vacío"
            );
        }

        return value;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDriverId() {
        return driverId;
    }

    public OrderViewState getState() {
        return state;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}
