import { Eye, XCircle, MoreHorizontal } from 'lucide-react';
import StatusBadge  from '../common/StatusBadge.jsx';
import { formatTime } from '../../utils/formatDate.js';

/** Mapea driver_id a foto del repartidor en /public/images/drivers/ */
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

/** Ícono de producto según el índice del pedido (rotación simple) */
const PRODUCT_IMGS = [
  '/images/products/hamburguesa.png',
  '/images/products/pizza.png',
  '/images/products/sushi.png',
  '/images/products/papas.png',
];
function productImg(index) {
  return PRODUCT_IMGS[index % PRODUCT_IMGS.length];
}

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
        No se pudo cargar la tabla.<br />
        <small>{error}</small>
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
            <th>Cliente</th>
            <th>Dirección</th>
            <th>Repartidor</th>
            <th>Estado</th>
            <th>Total</th>
            <th>Hora</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order, i) => {
            const avatarSrc = driverAvatar(order.driver_id);
            const imgSrc    = productImg(i);
            return (
              <tr key={order.order_id}>

                {/* # Pedido con foto de producto */}
                <td>
                  <div className="order-id-cell">
                    <img
                      src={imgSrc}
                      alt=""
                      className="order-product-img"
                      onError={e => { e.currentTarget.style.display = 'none'; }}
                    />
                    <span className="order-id-badge">
                      {order.order_id.slice(0, 8).toUpperCase()}
                    </span>
                  </div>
                </td>

                {/* Cliente */}
                <td className="order-client">
                  {order.customer_id ?? '—'}
                </td>

                {/* Dirección */}
                <td className="order-address">
                  <span className="address-pin">📍</span>
                  {order.delivery_address ?? '—'}
                </td>

                {/* Repartidor con foto */}
                <td>
                  <span className="driver-chip">
                    {avatarSrc ? (
                      <img
                        src={avatarSrc}
                        alt={order.driver_id}
                        className="driver-avatar-img"
                        onError={e => { e.currentTarget.style.display = 'none'; }}
                      />
                    ) : (
                      <span className="driver-avatar">
                        {order.driver_id?.[0]?.toUpperCase() ?? '?'}
                      </span>
                    )}
                    {order.driver_id ?? '—'}
                  </span>
                </td>

                {/* Estado */}
                <td><StatusBadge status={order.state} /></td>

                {/* Total */}
                <td className="order-total">
                  {order.total_amount
                    ? `$ ${Number(order.total_amount).toLocaleString('es-CO')}`
                    : '—'}
                </td>

                {/* Hora */}
                <td className="order-time">{formatTime(order.last_updated)}</td>

                {/* Acciones */}
                <td className="order-actions">
                  <button className="icon-btn" title="Ver detalle"
                    onClick={() => onViewDetail(order.order_id)}>
                    <Eye size={15} />
                  </button>
                  <button className="icon-btn icon-btn--danger" title="Cancelar"
                    onClick={() => onCancel(order.order_id)}>
                    <XCircle size={15} />
                  </button>
                  <button className="icon-btn" title="Más opciones">
                    <MoreHorizontal size={15} />
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
