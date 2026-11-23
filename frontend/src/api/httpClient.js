/**
 * httpClient.js
 * SRP: única responsabilidad — ejecutar fetch y traducir errores HTTP
 * a mensajes legibles en español.
 */

export async function request(url, options = {}) {
  let response;
  try {
    response = await fetch(url, options);
  } catch {
    throw new Error(
      'No se pudo conectar con el servidor. Verifica que el backend esté iniciado.'
    );
  }

  if (!response.ok) {
    if (response.status === 401) throw new Error('Correo o contraseña incorrectos.');
    if (response.status === 403) throw new Error('No tienes permisos para acceder.');
    throw new Error(`Error del servidor (HTTP ${response.status}).`);
  }

  return response.json();
}

export async function authorizedRequest(url, options = {}, token) {
  return request(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${token}`,
    },
  });
}
