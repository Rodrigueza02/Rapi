/**
 * OrderDetailModal.jsx
 * SRP: única responsabilidad — mostrar el detalle de un pedido.
 * Recibe el orderId, hace fetch internamente vía useOrderDetail.
 */

import Modal        from '../common/Modal.jsx';
import StatusBadge  from '../common/StatusBadge.jsx';
import useOrderDetail from '../../hooks/useOrderDetail.js';
import { formatTime, formatDate } from '../../utils/formatDate.js';

export default function OrderDetailModal({ orderId, onClose }) {
  const { order, loading, error } = useOrderDetail(orderId);

  return (
    <Modal title="Detalle del pedido" onClose={onClose} size="sm">
      {loading && (
        <div className="modal-loading">
          <span className="loading-spinner" />
          Cargando pedido...
        </div>
      )}

      {error && (
        <p className="form-error" role="alert">{error}</p>
      )}

      {order && !loading && (
        <dl className="order-detail-list">
          <div className="order-detail-row">
            <dt>ID del pedido</dt>
            <dd className="order-id-badge">{order.order_id}</dd>
          </div>
          <div className="order-detail-row">
            <dt>Repartidor</dt>
            <dd>
              <span className="driver-chip">
                <span className="driver-avatar">{order.driver_id?.[0] ?? '?'}</span>
                {order.driver_id ?? '—'}
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
      )}
    </Modal>
  );
}
