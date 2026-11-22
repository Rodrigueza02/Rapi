/**
 * authService.js
 * SRP: única responsabilidad — operaciones de autenticación contra el backend.
 * Delega almacenamiento a sessionStorage.js y HTTP a httpClient.js.
 */

import { request, authorizedRequest } from '../api/httpClient.js';
import { saveSession, clearSession, readSession } from './session/sessionStorage.js';

export { readSession, clearSession };

export async function signIn(email, password, remember = false) {
  const data = await request('/api/auth/login', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ email: email.trim().toLowerCase(), password }),
  });

  if (!data.access_token || !data.rol) {
    throw new Error('Respuesta de autenticación incompleta.');
  }

  const session = {
    token:     data.access_token,
    role:      data.rol,
    name:      data.nombre,
    email:     data.email,
    expiresAt: Date.now() + Number(data.expires_in || 3600) * 1000,
    remember,
  };

  saveSession(session);
  return session;
}

export async function verifySession(session) {
  if (!session) return null;
  try {
    const profile = await authorizedRequest('/api/auth/me', {}, session.token);
    return {
      ...session,
      name: profile.nombre || session.name,
      role: profile.rol    || session.role,
    };
  } catch {
    clearSession();
    return null;
  }
}

export async function authorizedFetch(url, options = {}) {
  const session = readSession();
  if (!session) throw new Error('Debes iniciar sesión para realizar esta acción.');
  return fetch(url, {
    ...options,
    headers: { ...options.headers, Authorization: `Bearer ${session.token}` },
  });
}
