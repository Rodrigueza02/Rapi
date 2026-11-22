import { useCallback, useState } from 'react';
import { Package } from 'lucide-react';

import DashboardStats      from '../../components/dashboard/DashboardStats.jsx';
import OrdersToolbar       from '../../components/orders/OrdersToolbar.jsx';
import OrdersTable         from '../../components/orders/OrdersTable.jsx';
import RoundRobinWheel     from '../../components/dashboard/RoundRobinWheel.jsx';
import RoundRobinNextCard  from '../../components/dashboard/RoundRobinNextCard.jsx';
import RequestsPanel       from '../../components/dashboard/RequestsPanel.jsx';
import PaymentsPanel       from '../../components/dashboard/PaymentsPanel.jsx';
import EmailsPanel         from '../../components/dashboard/EmailsPanel.jsx';

import useOrders      from '../../hooks/useOrders.js';
import useRoundRobin  from '../../hooks/useRoundRobin.js';
import useWebSocket   from '../../hooks/useWebSocket.js';

/**
 * Página principal del panel administrativo.
 *
 * Responsabilidades:
 *  - Orquestar los hooks de datos
 *  - Escuchar eventos WebSocket y refrescar la tabla/stats
 *  - Pasar datos hacia abajo a cada componente
 *
 * Principio SRP: cada componente hijo sabe solo renderizar su sección.
 */
export default function AdminDashboard() {
  const [search, setSearch] = useState('');

  // ── Datos del backend ──────────────────────────────────────────────────────
  const { orders, loading: ordersLoading, error: ordersError, reload: reloadOrders } = useOrders();
  const { roundRobin, loading: rrLoading, reload: reloadRR } = useRoundRobin();

  // ── WebSocket — refresca tabla y rueda cuando llega un evento ─────────────
  const onWsEvent = useCallback(() => {
    reloadOrders();
    reloadRR();
  }, [reloadOrders, reloadRR]);

  useWebSocket(onWsEvent);

  // ── Filtrado local por búsqueda ────────────────────────────────────────────
  const filtered = orders.filter(o =>
    !search ||
    o.order_id.toLowerCase().includes(search.toLowerCase()) ||
    o.driver_id?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="admin-dashboard">

      {/* ── Tarjetas de métricas ─────────────────────────────────────────── */}
      <DashboardStats />

      {/* ── Tabla de pedidos recientes ───────────────────────────────────── */}
      <section className="dashboard-section">
        <div className="section-heading">
          <div className="section-title">
            <Package size={20} />
            <h2>Pedidos recientes</h2>
          </div>
          <OrdersToolbar
            search={search}
            onSearch={setSearch}
            onNewOrder={() => {/* Segunda entrega */}}
          />
        </div>

        <OrdersTable
          orders={filtered}
          loading={ordersLoading}
          error={ordersError}
        />
      </section>

      {/* ── Round Robin + Solicitudes ────────────────────────────────────── */}
      <div className="dashboard-row">
        <section className="dashboard-section dashboard-section--rr">
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
            <RoundRobinWheel  roundRobin={roundRobin} />
            <RoundRobinNextCard roundRobin={roundRobin} loading={rrLoading} />
          </div>
        </section>

        <RequestsPanel requests={[]} />
      </div>

      {/* ── Pagos + Correos ──────────────────────────────────────────────── */}
      <div className="dashboard-row">
        <PaymentsPanel stats={null} />
        <EmailsPanel   stats={null} />
      </div>

    </div>
  );
}
