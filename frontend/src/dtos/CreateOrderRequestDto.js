/**
 * CreateOrderRequestDto.js
 * Shape exacto que espera POST /api/order en el backend.
 * Campos requeridos según CreateOrderRequest.java:
 *   customer_id, customer_phone, delivery_address, items[], total_amount
 */

export function buildCreateOrderRequest({ customerId, customerPhone, deliveryAddress, items, totalAmount }) {
  return {
    customer_id:      customerId,
    customer_phone:   customerPhone,
    delivery_address: deliveryAddress,
    items,
    total_amount:     Number(totalAmount),
  };
}
