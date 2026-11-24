/**
 * Centraliza el acceso a variables de entorno.
 * Importa siempre desde aquí, nunca uses import.meta.env directamente.
 *
 * Para sobreescribir en producción crea un archivo .env.production:
 *   VITE_API_BASE_URL=https://api.tudominio.com
 */
export const ENV = {
  /** Base URL del API. Vacío = usa el proxy de Vite (desarrollo). */
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL ?? '',

  /** Nombre de la app mostrado en el título. */
  APP_NAME: import.meta.env.VITE_APP_NAME ?? 'DeliveryFlow',

  /** true cuando estamos en desarrollo local. */
  IS_DEV: import.meta.env.DEV === true,
};
