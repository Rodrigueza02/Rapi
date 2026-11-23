/**
 * HttpDashboardApi.js
 * SRP: única responsabilidad — llamadas HTTP a /api/dashboard/*
 * Puerto dashboard-aggregator: 8085
 */

import { authorizedFetch } from '../services/authService.js';

async function parseJson(response) {
  if (!response.ok) throw new Error(`Error del servidor (HTTP ${response.status})`);
  return response.json();
}

export async function fetchStats() {
  const res = await authorizedFetch('/api/dashboard/stats');
  return parseJson(res);
}

export async function fetchOrders() {
  const res = await authorizedFetch('/api/dashboard/orders');
  return parseJson(res);
}

export async function fetchOrderById(orderId) {
  const res = await authorizedFetch(`/api/dashboard/orders/${orderId}`);
  return parseJson(res);
}

export async function fetchRoundRobin() {
  const res = await authorizedFetch('/api/dashboard/round-robin');
  return parseJson(res);
}
