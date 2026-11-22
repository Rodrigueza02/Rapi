import { Eye, MapPin, XCircle, MoreHorizontal } from 'lucide-react';
import StatusBadge from '../common/StatusBadge.jsx';
import { formatTime } from '../../utils/formatDate.js';

/**
 * Tabla de pedidos recientes.
 * Recibe los datos ya filtrados desde el padre.
 * No hace fetch — el hook useOrders vive en AdminDashboard.
 */
export default function OrdersTable({ orders, loading, error }) {
  if (loading) {
    return (
      <div className="table-state">
        <span className="loading-spinner" />
        Cargando pedidos...
      </div>
    );
  }

  if (error) {
    return (
      <div className="table-state table-state--error">
        No se pudo cargar la tabla de pedidos.<br />
        <small>{error}</small>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="table-state">
        No hay pedidos registrados aún.
      </div>
    );
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
              <td className="order-id">
                <span className="order-id-badge">
                  {order.order_id.slice(0, 8).toUpperCase()}
                </span>
              </td>
              <td className="order-driver">
                <span className="driver-chip">
                  <span className="driver-avatar" aria-hidden="true">
                    {order.driver_id?.[0] ?? '?'}
                  </span>
                  {order.driver_id ?? '—'}
                </span>
              </td>
              <td>
                <StatusBadge status={order.state} />
              </td>
              <td className="order-time">
                {formatTime(order.last_updated)}
              </td>
              <td className="order-actions">
                <button className="icon-btn" title="Ver detalle">
                  <Eye size={16} />
                </button>
                <button className="icon-btn" title="Ver en mapa">
                  <MapPin size={16} />
                </button>
                <button className="icon-btn icon-btn--danger" title="Cancelar">
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
