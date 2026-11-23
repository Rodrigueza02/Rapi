/**
 * orderStatus.js
 * Mapas de estados del backend → etiquetas y clases CSS.
 * El backend puede enviar ARRIVING o ON_THE_WAY — ambos se manejan.
 * No se modifica el backend; solo se amplía el mapeo en el frontend.
 */

export const ORDER_STATUS = {
  CREATED:    'CREATED',
  ASSIGNED:   'ASSIGNED',
  ARRIVING:   'ARRIVING',
  ON_THE_WAY: 'ON_THE_WAY',  /* alias que algunos eventos del backend usan */
  DELIVERED:  'DELIVERED',
  CANCELLED:  'CANCELLED',
};

export const STATUS_LABEL = {
  [ORDER_STATUS.CREATED]:    'Creado',
  [ORDER_STATUS.ASSIGNED]:   'Asignado',
  [ORDER_STATUS.ARRIVING]:   'En camino',
  [ORDER_STATUS.ON_THE_WAY]: 'En camino',
  [ORDER_STATUS.DELIVERED]:  'Entregado',
  [ORDER_STATUS.CANCELLED]:  'Cancelado',
};

export const STATUS_COLOR = {
  [ORDER_STATUS.CREATED]:    'status--created',
  [ORDER_STATUS.ASSIGNED]:   'status--assigned',
  [ORDER_STATUS.ARRIVING]:   'status--arriving',
  [ORDER_STATUS.ON_THE_WAY]: 'status--arriving',  /* mismo estilo naranja */
  [ORDER_STATUS.DELIVERED]:  'status--delivered',
  [ORDER_STATUS.CANCELLED]:  'status--cancelled',
};
