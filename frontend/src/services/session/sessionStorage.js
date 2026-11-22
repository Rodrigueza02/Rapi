/**
 * sessionStorage.js
 * SRP: única responsabilidad — leer y borrar la sesión del navegador.
 */

const STORAGE_KEY = 'deliveryflow_auth';

export function readSession() {
  try {
    const stored = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || 'null');
    return stored && stored.expiresAt > Date.now() ? stored : null;
  } catch {
    return null;
  }
}

export function saveSession(session) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession() {
  sessionStorage.removeItem(STORAGE_KEY);
}
