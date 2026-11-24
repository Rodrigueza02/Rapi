/**
 * AdminDashboard.jsx
 *
 * Única fuente de datos para el dashboard: controla todos los fetches
 * y los refresca en tiempo real cuando llegan eventos vía WebSocket.
 *
 * WEBSOCKET
 * ---------
 * - Una sola conexión (useWebSocket abre exactamente un socket por ciclo de vida).
 * - onWsEvent es estable (useCallback con refs de funciones también estables).
 * - wsStatus se reporta al AdminLayout a través de useOutletContext para que el
 *   header muestre el indicador de conexión real sin abrir un segundo socket.
 *
 * REFRESCO DE DATOS
 * -----------------
 * Cuando llega cualquier evento WS se refrescan pedidos, stats y round-robin
 * con fetch REST al aggregator (fuente de verdad).
 */
import { useCallback, useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Package } from 'lucide-react';

import DashboardStats     from '../../components/dashboard/DashboardStats.jsx';
import OrdersToolbar      from '../../components/orders/OrdersToolbar.jsx';
import OrdersTable        from '../../components/orders/OrdersTable.jsx';
import OrderDetailModal   from '../../components/orders/OrderDetailModal.jsx';
import CreateOrderModal   from '../../components/orders/CreateOrderModal.jsx';
import CancelOrderModal   from '../../components/orders/CancelOrderModal.jsx';
import RoundRobinWheel    from '../../components/dashboard/RoundRobinWheel.jsx';
import RoundRobinNextCard from '../../components/dashboard/RoundRobinNextCard.jsx';

import useOrders         from '../../hooks/useOrders.js';
import useRoundRobin     from '../../hooks/useRoundRobin.js';
import useDashboardStats from '../../hooks/useDashboardStats.js';
import useWebSocket      from '../../hooks/useWebSocket.js';

export default function AdminDashboard() {
  const [search,          setSearch]          = useState('');
  const [detailOrderId,   setDetailOrderId]   = useState(null);
  const [cancelOrderId,   setCancelOrderId]   = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  /* ── Datos del backend ──────────────────────────────────────────────────── */
  const { orders, loading: ordersLoading, error, reload: reloadOrders } = useOrders();
  const { roundRobin, loading: rrLoading, reload: reloadRR }            = useRoundRobin();
  const { stats, loading: statsLoading, reload: reloadStats }           = useDashboardStats();

  /* ── Outlet context: reportar estado WS al layout ──────────────────────── */
  // useOutletContext puede devolver undefined si el componente se usa fuera del layout.
  const outletCtx = useOutletContext?.() ?? {};
  const reportWsStatus = outletCtx.reportWsStatus ?? (() => {});

  /* ── WebSocket ──────────────────────────────────────────────────────────── */
  // onWsEvent: refresca los tres orígenes de datos al llegar cualquier evento.
  // Es estable porque reloadOrders, reloadStats y reloadRR son useCallback.
  const onWsEvent = useCallback(() => {
    reloadOrders();
    reloadStats();
    reloadRR();
  }, [reloadOrders, reloadStats, reloadRR]);

  const { wsStatus } = useWebSocket(onWsEvent);

  // Propagar el estado de conexión al header del layout.
  useEffect(() => { reportWsStatus(wsStatus); }, [wsStatus, reportWsStatus]);

  /* ── Filtrado de pedidos ────────────────────────────────────────────────── */
  const filtered = orders.filter(o =>
    !search ||
    o.order_id?.toLowerCase().includes(search.toLowerCase()) ||
    o.driver_id?.toLowerCase().includes(search.toLowerCase()) ||
    o.customer_id?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="admin-dashboard">

      {/* Métricas + tarjetas de estado */}
      <DashboardStats
        stats={stats}
        orders={orders}
        loading={statsLoading || ordersLoading}
      />

      {/* Tabla de pedidos */}
      <section className="dashboard-section">
        <div className="section-heading">
          <div className="section-title">
            <Package size={20} />
            <h2>Pedidos recientes</h2>
          </div>
          <OrdersToolbar
            search={search}
            onSearch={setSearch}
            onNewOrder={() => setShowCreateModal(true)}
          />
        </div>
        <OrdersTable
          orders={filtered}
          loading={ordersLoading}
          error={error}
          onViewDetail={setDetailOrderId}
          onCancel={setCancelOrderId}
        />
      </section>

      {/* Round Robin */}
      <div className="dashboard-row">
        <section className="dashboard-section">
          <div className="section-heading">
            <div className="section-title">
              <span className="rr-icon-title">🔄</span>
              <div>
                <h2>Asignación de repartidores (Round Robin)</h2>
                <p>Rotación automática y equitativa de pedidos</p>
              </div>
            </div>
          </div>
          <div className="rr-layout">
            <RoundRobinWheel    roundRobin={roundRobin} />
            <RoundRobinNextCard roundRobin={roundRobin} loading={rrLoading} />
          </div>
        </section>
      </div>

      {/* Modales */}
      {detailOrderId && (
        <OrderDetailModal
          orderId={detailOrderId}
          onClose={() => setDetailOrderId(null)}
        />
      )}
      {cancelOrderId && (
        <CancelOrderModal
          orderId={cancelOrderId}
          onClose={() => setCancelOrderId(null)}
          onCancelled={() => { reloadOrders(); setCancelOrderId(null); }}
        />
      )}
      {showCreateModal && (
        <CreateOrderModal
          onClose={() => setShowCreateModal(false)}
          onCreated={reloadOrders}
        />
      )}
    </div>
  );
}
