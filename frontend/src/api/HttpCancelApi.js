/**
 * HttpCancelApi.js
 * SRP: única responsabilidad — cancelar un pedido vía DELETE /api/order/{id}
 */

import { authorizedFetch } from '../services/authService.js';

export async function cancelOrder(orderId) {
  const res = await authorizedFetch(`/api/order/${orderId}`, {
    method: 'DELETE',
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `No se pudo cancelar el pedido (HTTP ${res.status})`);
  }
  return res.json();
}
