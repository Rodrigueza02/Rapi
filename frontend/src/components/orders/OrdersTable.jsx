/**
 * OrdersTable.jsx
 * SRP: única responsabilidad — renderizar la tabla y notificar acciones al padre.
 */

import { Eye, MapPin, XCircle, MoreHorizontal } from 'lucide-react';
import StatusBadge from '../common/StatusBadge.jsx';
import { formatTime } from '../../utils/formatDate.js';

export default function OrdersTable({ orders, loading, error, onViewDetail, onCancel }) {
  if (loading) {
    return (
      <div className="table-state">
        <span className="loading-spinner" /> Cargando pedidos...
      </div>
    );
  }

  if (error) {
    return (
      <div className="table-state table-state--error">
        No se pudo cargar la tabla.<br /><small>{error}</small>
      </div>
    );
  }

  if (orders.length === 0) {
    return <div className="table-state">No hay pedidos registrados aún.</div>;
  }

  return (
    <div className="table-wrap">
      <table className="orders-table">
        <thead>
          <tr>
            <th># Pedido</th>
            <th>Repartidor</th>
            <th>Estado</th>
            <th>Hora</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.order_id}>
              <td>
                <span className="order-id-badge">
                  {order.order_id.slice(0, 8).toUpperCase()}
                </span>
              </td>
              <td>
                <span className="driver-chip">
                  <span className="driver-avatar">{order.driver_id?.[0] ?? '?'}</span>
                  {order.driver_id ?? '—'}
                </span>
              </td>
              <td><StatusBadge status={order.state} /></td>
              <td className="order-time">{formatTime(order.last_updated)}</td>
              <td className="order-actions">
                <button className="icon-btn" title="Ver detalle"
                  onClick={() => onViewDetail(order.order_id)}>
                  <Eye size={16} />
                </button>
                <button className="icon-btn" title="Ver en mapa">
                  <MapPin size={16} />
                </button>
                <button className="icon-btn icon-btn--danger" title="Cancelar"
                  onClick={() => onCancel(order.order_id)}>
                  <XCircle size={16} />
                </button>
                <button className="icon-btn" title="Más opciones">
                  <MoreHorizontal size={16} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
