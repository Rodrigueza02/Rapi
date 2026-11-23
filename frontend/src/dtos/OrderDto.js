/**
 * OrderDto.js
 * Representa el shape exacto que devuelve GET /api/dashboard/orders
 * y GET /api/dashboard/orders/{id}
 *
 * { order_id, driver_id, state, last_updated }
 */

/** Convierte el DTO crudo a un objeto normalizado para la UI */
export function normalizeOrder(raw) {
  return {
    orderId:     raw.order_id,
    driverId:    raw.driver_id,
    state:       raw.state,
    lastUpdated: raw.last_updated,
  };
}
