/**
 * AdminDashboard.jsx — sin tarjetas simuladas, con ciclo cronológico real.
 */
import { useCallback, useState } from 'react';
import { Package } from 'lucide-react';

import DashboardStats     from '../../components/dashboard/DashboardStats.jsx';
import OrdersToolbar      from '../../components/orders/OrdersToolbar.jsx';
import OrdersTable        from '../../components/orders/OrdersTable.jsx';
import OrderDetailModal   from '../../components/orders/OrderDetailModal.jsx';
import CreateOrderModal   from '../../components/orders/CreateOrderModal.jsx';
import CancelOrderModal   from '../../components/orders/CancelOrderModal.jsx';
import RoundRobinWheel    from '../../components/dashboard/RoundRobinWheel.jsx';
import RoundRobinNextCard from '../../components/dashboard/RoundRobinNextCard.jsx';

import useOrders     from '../../hooks/useOrders.js';
import useRoundRobin from '../../hooks/useRoundRobin.js';
import useWebSocket  from '../../hooks/useWebSocket.js';

export default function AdminDashboard() {
  const [search,          setSearch]          = useState('');
  const [detailOrderId,   setDetailOrderId]   = useState(null);
  const [cancelOrderId,   setCancelOrderId]   = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  const { orders, loading, error, reload: reloadOrders } = useOrders();
  const { roundRobin, loading: rrLoading, reload: reloadRR } = useRoundRobin();

  const onWsEvent = useCallback(() => {
    reloadOrders();
    reloadRR();
  }, [reloadOrders, reloadRR]);

  useWebSocket(onWsEvent);

  const filtered = orders.filter(o =>
    !search ||
    o.order_id?.toLowerCase().includes(search.toLowerCase()) ||
    o.driver_id?.toLowerCase().includes(search.toLowerCase()) ||
    o.customer_id?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="admin-dashboard">

      {/* Métricas + tarjetas de estado */}
      <DashboardStats />

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
          loading={loading}
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
