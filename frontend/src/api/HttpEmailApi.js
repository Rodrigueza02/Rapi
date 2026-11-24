import { authorizedFetch } from '../services/authService.js';

async function parseJson(res) {
  if (!res.ok) throw new Error(`Error del servidor (HTTP ${res.status})`);
  return res.json();
}

export async function fetchEmails({ status = '', page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page, size });
  if (status) params.set('status', status);
  const res = await authorizedFetch(`/api/emails?${params}`);
  return parseJson(res);
}

export async function fetchEmailById(commandId) {
  const res = await authorizedFetch(`/api/emails/${commandId}`);
  return parseJson(res);
}

export async function fetchEmailStats() {
  const res = await authorizedFetch('/api/emails/stats');
  return parseJson(res);
}

export async function fetchEmailsByOrder(orderId) {
  const res = await authorizedFetch(`/api/emails/order/${orderId}`);
  return parseJson(res);
}

export async function sendEmail({ recipientEmail, subject, bodyText, orderId }) {
  const res = await authorizedFetch('/api/emails/send', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ recipientEmail, subject, bodyText, orderId }),
  });
  return parseJson(res);
}
