import { Eye, XCircle } from 'lucide-react';
import StatusBadge  from '../common/StatusBadge.jsx';
import { formatTime } from '../../utils/formatDate.js';
import { NON_CANCELLABLE_STATES } from '../../constants/orderStatus.js';

function driverAvatar(driverId) {
  const map = {
    'driver-1': '/images/drivers/rep-01.png',
    'driver-2': '/images/drivers/rep-02.png',
    'driver-3': '/images/drivers/rep-03.png',
    'driver-4': '/images/drivers/rep-04.png',
    'driver-5': '/images/drivers/rep-05.png',
  };
  return map[driverId] ?? null;
}

const PRODUCT_IMGS = [
  '/images/products/hamburguesa.png',
  '/images/products/pizza.png',
  '/images/products/sushi.png',
  '/images/products/papas.png',
];

function parseItems(itemsJson) {
  if (!itemsJson) return null;
  try {
    const arr = JSON.parse(itemsJson);
    return Array.isArray(arr) ? arr.join(', ') : itemsJson;
  } catch { return itemsJson; }
}

export default function OrdersTable({ orders, loading, error, onViewDetail, onCancel }) {
  if (loading) return <div className="table-state"><span className="loading-spinner" /> Cargando pedidos...</div>;
  if (error)   return <div className="table-state table-state--error">No se pudo cargar la tabla.<br /><small>{error}</small></div>;
  if (orders.length === 0) return <div className="table-state">No hay pedidos registrados aún.</div>;

  return (
    <div className="table-wrap">
      <table className="orders-table">
        <thead>
          <tr>
            <th># Pedido</th>
            <th>Cliente</th>
            <th>Dirección</th>
            <th>Productos</th>
            <th>Repartidor</th>
            <th>Estado</th>
            <th>Total</th>
            <th>Hora</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order, i) => {
            const avatarSrc  = driverAvatar(order.driver_id);
            const imgSrc     = PRODUCT_IMGS[i % PRODUCT_IMGS.length];
            const canCancel  = !NON_CANCELLABLE_STATES?.has(order.state);
            const items      = parseItems(order.items_json);

            return (
              <tr key={order.order_id}>
                <td>
                  <div className="order-id-cell">
                    <img src={imgSrc} alt="" className="order-product-img"
                         onError={e => { e.currentTarget.style.display = 'none'; }} />
                    <span className="order-id-badge">{order.order_id.slice(0, 8).toUpperCase()}</span>
                  </div>
                </td>
                <td className="order-client">{order.customer_id ?? '—'}</td>
                <td className="order-address">
                  <span className="address-pin">📍</span>
                  {order.delivery_address ?? '—'}
                </td>
                <td className="order-items">{items ?? '—'}</td>
                <td>
                  <span className="driver-chip">
                    {avatarSrc
                      ? <img src={avatarSrc} alt={order.driver_id} className="driver-avatar-img"
                             onError={e => { e.currentTarget.style.display = 'none'; }} />
                      : <span className="driver-avatar">{order.driver_id?.[0]?.toUpperCase() ?? '?'}</span>
                    }
                    {order.driver_id ?? '—'}
                  </span>
                </td>
                <td><StatusBadge status={order.state} /></td>
                <td className="order-total">
                  {order.total_amount ? `$ ${Number(order.total_amount).toLocaleString('es-CO')}` : '—'}
                </td>
                <td className="order-time">{formatTime(order.last_updated)}</td>
                <td className="order-actions">
                  <button className="icon-btn" title="Ver detalle"
                          onClick={() => onViewDetail(order.order_id)}>
                    <Eye size={15} />
                  </button>
                  <button
                    className={`icon-btn ${canCancel ? 'icon-btn--danger' : 'icon-btn--disabled'}`}
                    title={canCancel ? 'Cancelar' : 'No cancelable'}
                    onClick={() => onCancel(order.order_id)}
                    disabled={!canCancel}
                  >
                    <XCircle size={15} />
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
