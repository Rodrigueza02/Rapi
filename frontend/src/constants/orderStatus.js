/**
 * orderStatus.js — mapeo completo de estados del backend.
 * Ciclo: CREATED → PREPARED → ASSIGNED → ON_THE_WAY → ARRIVING → DELIVERED
 * Terminal alternativo: CANCELLED
 */

export const ORDER_STATUS = {
  CREATED:    'CREATED',
  PREPARED:   'PREPARED',
  ASSIGNED:   'ASSIGNED',
  ON_THE_WAY: 'ON_THE_WAY',
  ARRIVING:   'ARRIVING',
  DELIVERED:  'DELIVERED',
  CANCELLED:  'CANCELLED',
};

/** Estados activos (no terminales) */
export const ACTIVE_STATES = new Set([
  'CREATED', 'PREPARED', 'ASSIGNED', 'ON_THE_WAY', 'ARRIVING',
]);

export const STATUS_LABEL = {
  CREATED:    'Creado',
  PREPARED:   'En preparación',
  ASSIGNED:   'Asignado',
  ON_THE_WAY: 'En camino',
  ARRIVING:   'Llegando',
  DELIVERED:  'Entregado',
  CANCELLED:  'Cancelado',
};

export const STATUS_COLOR = {
  CREATED:    'status--created',
  PREPARED:   'status--prepared',
  ASSIGNED:   'status--assigned',
  ON_THE_WAY: 'status--on-the-way',
  ARRIVING:   'status--arriving',
  DELIVERED:  'status--delivered',
  CANCELLED:  'status--cancelled',
};

/** Orden cronológico para tarjetas del dashboard */
export const STATE_SEQUENCE = [
  'CREATED', 'PREPARED', 'ASSIGNED', 'ON_THE_WAY', 'ARRIVING', 'DELIVERED',
];

/** Estados que NO permiten cancelación */
export const NON_CANCELLABLE_STATES = new Set(['DELIVERED', 'CANCELLED', 'ON_THE_WAY', 'ARRIVING']);
