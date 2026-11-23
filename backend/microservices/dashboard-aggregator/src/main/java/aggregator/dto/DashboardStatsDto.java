package aggregator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Métricas que el dashboard muestra en las 4 tarjetas superiores.
 *
 * active    → pedidos en estado ASSIGNED o ARRIVING (en curso)
 * delivered → pedidos en estado DELIVERED
 * total     → todos los pedidos conocidos por el agregador
 */
public record DashboardStatsDto(
        @JsonProperty("active_orders")    long activeOrders,
        @JsonProperty("delivered_orders") long deliveredOrders,
        @JsonProperty("total_orders")     long totalOrders,
        @JsonProperty("total_drivers")    long totalDrivers
) {}
