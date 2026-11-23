/**
 * HttpOrderApi.js
 * SRP: única responsabilidad — llamadas HTTP a /api/order/*
 * Puerto order-api: 8081
 */

import { authorizedFetch } from '../services/authService.js';
import { buildCreateOrderRequest } from '../dtos/CreateOrderRequestDto.js';

async function parseJson(response) {
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || `Error del servidor (HTTP ${response.status})`);
  }
  return response.json();
}

/**
 * POST /api/order
 * @param {object} formData — { customerId, customerPhone, deliveryAddress, items, totalAmount }
 * @returns {{ order_id, status, message }}
 */
export async function createOrder(formData) {
  const body = buildCreateOrderRequest(formData);
  const res  = await authorizedFetch('/api/order', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify(body),
  });
  return parseJson(res);
}
