import { authorizedFetch } from '../services/authService.js';

/**
 * Implementación HTTP del puerto DashboardApiPort.
 * Principio SRP: solo sabe cómo hablar con /api/dashboard/*.
 * Principio DIP: los hooks dependen del puerto, no de este archivo directamente.
 */

async function parseJson(response) {
  if (!response.ok) {
    throw new Error(`Error del servidor (HTTP ${response.status})`);
  }
  return response.json();
}

/** GET /api/dashboard/stats → { active_orders, delivered_orders, total_orders, total_drivers } */
export async function fetchStats() {
  const res = await authorizedFetch('/api/dashboard/stats');
  return parseJson(res);
}

/** GET /api/dashboard/orders → OrderViewDto[] */
export async function fetchOrders() {
  const res = await authorizedFetch('/api/dashboard/orders');
  return parseJson(res);
}

/** GET /api/dashboard/round-robin → { drivers, current_index, next_driver_id, turn_label } */
export async function fetchRoundRobin() {
  const res = await authorizedFetch('/api/dashboard/round-robin');
  return parseJson(res);
}
