export const ORDER_STATUS = {
  CREATED:   'CREATED',
  ASSIGNED:  'ASSIGNED',
  ARRIVING:  'ARRIVING',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
};

export const STATUS_LABEL = {
  [ORDER_STATUS.CREATED]:   'Creado',
  [ORDER_STATUS.ASSIGNED]:  'Asignado',
  [ORDER_STATUS.ARRIVING]:  'En camino',
  [ORDER_STATUS.DELIVERED]: 'Entregado',
  [ORDER_STATUS.CANCELLED]: 'Cancelado',
};

export const STATUS_COLOR = {
  [ORDER_STATUS.CREATED]:   'status--created',
  [ORDER_STATUS.ASSIGNED]:  'status--assigned',
  [ORDER_STATUS.ARRIVING]:  'status--arriving',
  [ORDER_STATUS.DELIVERED]: 'status--delivered',
  [ORDER_STATUS.CANCELLED]: 'status--cancelled',
};
