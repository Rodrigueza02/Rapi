/**
 * Contenedor de dependencias manual (sin framework DI).
 *
 * Principio DIP: los módulos dependen de interfaces (ports),
 * y aquí se conectan con sus implementaciones concretas.
 *
 * Amplía este archivo cuando agregues nuevos servicios.
 */
import { ENV } from './env.js';

export const config = {
  apiBaseUrl: ENV.API_BASE_URL,
};
