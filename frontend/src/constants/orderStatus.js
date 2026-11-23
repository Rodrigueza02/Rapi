/**
 * Estados de pedido que devuelve el backend (OrderViewState.java).
 * El frontend los usa para etiquetas, colores y filtros.
 */
export const ORDER_STATUS = {
  ASSIGNED:  'ASSIGNED',
  ARRIVING:  'ARRIVING',
  DELIVERED: 'DELIVERED',
};

export const STATUS_LABEL = {
  [ORDER_STATUS.ASSIGNED]:  'Asignado',
  [ORDER_STATUS.ARRIVING]:  'En camino',
  [ORDER_STATUS.DELIVERED]: 'Entregado',
};

export const STATUS_COLOR = {
  [ORDER_STATUS.ASSIGNED]:  'status--assigned',
  [ORDER_STATUS.ARRIVING]:  'status--arriving',
  [ORDER_STATUS.DELIVERED]: 'status--delivered',
};
