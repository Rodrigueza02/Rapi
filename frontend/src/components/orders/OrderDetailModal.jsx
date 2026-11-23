/**
 * OrderDetailModal.jsx — muestra cliente, productos, dirección, total y línea de tiempo.
 */
import Modal            from '../common/Modal.jsx';
import StatusBadge      from '../common/StatusBadge.jsx';
import OrderStateHistory from './OrderStateHistory.jsx';
import useOrderDetail   from '../../hooks/useOrderDetail.js';
import { formatTime, formatDate } from '../../utils/formatDate.js';

function formatTotal(amount) {
  if (!amount) return '—';
  return `$ ${Number(amount).toLocaleString('es-CO')}`;
}

function parseItems(itemsJson) {
  if (!itemsJson) return null;
  try {
    const arr = JSON.parse(itemsJson);
    return Array.isArray(arr) ? arr.join(', ') : itemsJson;
  } catch { return itemsJson; }
}

export default function OrderDetailModal({ orderId, onClose }) {
  const { order, loading, error } = useOrderDetail(orderId);

  return (
    <Modal title="Detalle del pedido" onClose={onClose} size="md">
      {loading && (
        <div className="modal-loading">
          <span className="loading-spinner" /> Cargando pedido...
        </div>
      )}
      {error && <p className="form-error" role="alert">{error}</p>}

      {order && !loading && (
        <div className="order-detail-layout">
          <dl className="order-detail-list">
            <div className="order-detail-row">
              <dt>ID del pedido</dt>
              <dd className="order-id-badge">{order.order_id}</dd>
            </div>
            <div className="order-detail-row">
              <dt>Cliente</dt>
              <dd>{order.customer_id ?? 'Dato no disponible'}</dd>
            </div>
            <div className="order-detail-row">
              <dt>Dirección</dt>
              <dd>{order.delivery_address ?? 'Dato no disponible'}</dd>
            </div>
            <div className="order-detail-row">
              <dt>Productos</dt>
              <dd>{parseItems(order.items_json) ?? 'Dato no disponible'}</dd>
            </div>
            <div className="order-detail-row">
              <dt>Total</dt>
              <dd>{formatTotal(order.total_amount)}</dd>
            </div>
            <div className="order-detail-row">
              <dt>Repartidor</dt>
              <dd>
                <span className="driver-chip">
                  <span className="driver-avatar">{order.driver_id?.[0]?.toUpperCase() ?? '?'}</span>
                  {order.driver_id ?? 'Sin asignar'}
                </span>
              </dd>
            </div>
            <div className="order-detail-row">
              <dt>Estado</dt>
              <dd><StatusBadge status={order.state} /></dd>
            </div>
            <div className="order-detail-row">
              <dt>Última actualización</dt>
              <dd>{formatDate(order.last_updated)} {formatTime(order.last_updated)}</dd>
            </div>
          </dl>

          {/* Línea de tiempo */}
          <OrderStateHistory order={order} />
        </div>
      )}
    </Modal>
  );
}
