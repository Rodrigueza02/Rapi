package aggregator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vista de un repartidor derivada de los pedidos conocidos por el aggregator.
 *
 * driver_id          → identificador del repartidor (ej: driver-1)
 * status             → EN_RUTA si tiene pedido ASSIGNED o ARRIVING; DESCONOCIDO si no
 * active_order_id    → ID del pedido activo, null si no hay
 * active_order_state → ASSIGNED | ARRIVING, null si no hay pedido activo
 * completed_orders   → pedidos en estado DELIVERED asignados a este repartidor
 * total_orders       → total de pedidos asignados alguna vez (historial en memoria)
 * last_activity      → timestamp del último evento recibido para este repartidor
 */
public record DriverViewDto(
        @JsonProperty("driver_id")          String driverId,
        @JsonProperty("status")             String status,
        @JsonProperty("active_order_id")    String activeOrderId,
        @JsonProperty("active_order_state") String activeOrderState,
        @JsonProperty("completed_orders")   long   completedOrders,
        @JsonProperty("total_orders")       long   totalOrders,
        @JsonProperty("last_activity")      String lastActivity
) {}
