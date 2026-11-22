/**
 * authService.js
 *
 * Principio SRP: solo gestiona autenticación HTTP y sesión en sessionStorage.
 * No conoce React ni componentes.
 */

const STORAGE_KEY = 'deliveryflow_auth';

// ── Sesión ────────────────────────────────────────────────────────────────────

/** Lee la sesión guardada. Devuelve null si no existe o ya expiró. */
export function readSession() {
  try {
    const stored = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || 'null');
    return stored && stored.expiresAt > Date.now() ? stored : null;
  } catch {
    return null;
  }
}

/** Elimina la sesión del storage. */
export function clearSession() {
  sessionStorage.removeItem(STORAGE_KEY);
}

// ── Helpers HTTP ──────────────────────────────────────────────────────────────

/**
 * fetch con manejo de errores HTTP uniforme.
 * Lanza Error con mensaje legible en español.
 */
async function request(url, options = {}) {
  let response;
  try {
    response = await fetch(url, options);
  } catch {
    throw new Error(
      'No se pudo conectar con el servidor de autenticación. Verifica que el backend esté iniciado.'
    );
  }

  if (!response.ok) {
    if (response.status === 401) throw new Error('Correo o contraseña incorrectos.');
    if (response.status === 403) throw new Error('No tienes permisos para acceder.');
    throw new Error(`Error del servidor (HTTP ${response.status}).`);
  }

  return response.json();
}

// ── Operaciones de autenticación ──────────────────────────────────────────────

/**
 * Inicia sesión con email y password.
 * Guarda la sesión en sessionStorage y la devuelve.
 *
 * @param {string}  email
 * @param {string}  password
 * @param {boolean} remember  (guardado para uso futuro con localStorage)
 * @returns {Promise<Session>}
 */
export async function signIn(email, password, remember = false) {
  const data = await request('/api/auth/login', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({
      email:    email.trim().toLowerCase(),
      password,
    }),
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

  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  return session;
}

/**
 * Verifica que el token guardado siga siendo válido contra el backend.
 * Devuelve la sesión actualizada, o null si el token expiró.
 *
 * @param {Session|null} session
 * @returns {Promise<Session|null>}
 */
export async function verifySession(session) {
  if (!session) return null;
  try {
    const profile = await request('/api/auth/me', {
      headers: { Authorization: `Bearer ${session.token}` },
    });
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

/**
 * fetch con el header Authorization del token activo.
 * Lanza Error si no hay sesión válida.
 *
 * @param {string} url
 * @param {RequestInit} options
 * @returns {Promise<Response>}
 */
export async function authorizedFetch(url, options = {}) {
  const session = readSession();
  if (!session) throw new Error('Debes iniciar sesión para realizar esta acción.');
  return fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${session.token}`,
    },
  });
}
