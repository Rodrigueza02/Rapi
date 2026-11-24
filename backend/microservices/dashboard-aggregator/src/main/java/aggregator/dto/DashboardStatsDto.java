package aggregator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * active_orders    = pedidos en estados no terminales (CREATED/PREPARED/ASSIGNED/ON_THE_WAY/ARRIVING)
 * delivered_orders = pedidos DELIVERED
 * total_orders     = todos
 * total_drivers    = repartidores únicos vistos
 */
public record DashboardStatsDto(
        @JsonProperty("active_orders")    long activeOrders,
        @JsonProperty("delivered_orders") long deliveredOrders,
        @JsonProperty("total_orders")     long totalOrders,
        @JsonProperty("total_drivers")    long totalDrivers
) {}
