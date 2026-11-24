/**
 * PedidosPage.jsx
 * Pantalla de Gestión de Pedidos — /admin/pedidos
 *
 * Integración real:
 *   - useOrders      → lista de pedidos desde /api/dashboard/orders
 *   - useOrderDetail → detalle individual desde /api/dashboard/orders/:id
 *   - useWebSocket   → refresca la lista cuando llegan eventos en tiempo real
 *
 * Cambios respecto a versión anterior:
 *   - Eliminadas imágenes de productos (hamburguesa, pizza, etc.) — no
 *     corresponden a ningún producto real del pedido
 *   - Eliminados porcentajes de tendencia sin respaldo histórico
 *   - Métricas se calculan únicamente desde los pedidos reales disponibles
 */

import { useState, useCallback, useMemo } from 'react';
import {
  ShoppingBag, Truck, CheckCircle2, XCircle,
  Search, ChevronRight, Package,
} from 'lucide-react';

import useOrders      from '../../hooks/useOrders.js';
import useOrderDetail from '../../hooks/useOrderDetail.js';
import useWebSocket   from '../../hooks/useWebSocket.js';

import StatusBadge       from '../../components/common/StatusBadge.jsx';
import OrderTracker      from '../../components/orders/OrderTracker.jsx';
import OrderStateHistory from '../../components/orders/OrderStateHistory.jsx';
import OrdersTable       from '../../components/orders/OrdersTable.jsx';
import OrderDetailModal  from '../../components/orders/OrderDetailModal.jsx';
import CancelOrderModal  from '../../components/orders/CancelOrderModal.jsx';

const TABS      = ['Seguimiento en tiempo real', 'Listado de pedidos', 'Historial'];
const PAGE_SIZE = 5;

export default function PedidosPage() {

  /* ── Estado de UI ─────────────────────────────────────────── */
  const [activeTab,     setActiveTab]     = useState(0);
  const [search,        setSearch]        = useState('');
  const [selectedId,    setSelectedId]    = useState(null);
  const [cancelOrderId, setCancelOrderId] = useState(null);
  const [detailModalId, setDetailModalId] = useState(null);
  const [page,          setPage]          = useState(1);

  /* ── Datos del backend ────────────────────────────────────── */
  const { orders, loading: ordersLoading, error: ordersError, reload } = useOrders();
  const { order: selectedOrder, loading: detailLoading, error: detailError } =
    useOrderDetail(selectedId);

  const onWsEvent = useCallback(() => { reload(); }, [reload]);
  useWebSocket(onWsEvent);

  /* ── Métricas calculadas desde los pedidos reales ─────────── */
  const metrics = useMemo(() => ({
    total:     orders.length,
    arriving:  orders.filter(o => o.state === 'ARRIVING').length,
    delivered: orders.filter(o => o.state === 'DELIVERED').length,
    assigned:  orders.filter(o => o.state === 'ASSIGNED').length,
  }), [orders]);

  /* ── Filtrado y paginación ────────────────────────────────── */
  const filtered = useMemo(() => {
    if (!search.trim()) return orders;
    const q = search.toLowerCase();
    return orders.filter(o =>
      o.order_id?.toLowerCase().includes(q) ||
      o.driver_id?.toLowerCase().includes(q)
    );
  }, [orders, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  function handleSearch(val) { setSearch(val); setPage(1); }
  function handleCancelled() { reload(); setCancelOrderId(null); }

  /* ── Tarjetas de métricas ─────────────────────────────────── */
  const METRIC_CARDS = [
    { label: 'Pedidos totales', value: metrics.total,     icon: ShoppingBag,  color: 'orange' },
    { label: 'En camino',       value: metrics.arriving,  icon: Truck,         color: 'blue'   },
    { label: 'Entregados',      value: metrics.delivered, icon: CheckCircle2,  color: 'green'  },
    { label: 'Asignados',       value: metrics.assigned,  icon: Package,       color: 'red'    },
  ];

  return (
    <div className="admin-dashboard">

      {/* ── Métricas — valores reales, sin tendencias ficticias ── */}
      <section className="pedidos-stats" aria-label="Métricas de pedidos">
        {METRIC_CARDS.map(({ label, value, icon: Icon, color }) => (
          <article key={label} className={`stat-card stat-card--${color}`}>
            <div className="stat-top">
              <span className={`stat-icon stat-icon--${color}`}><Icon size={20} /></span>
            </div>
            <span className="stat-label">{label}</span>
            <strong className="stat-value">
              {ordersLoading ? <span className="stat-skeleton" /> : value}
            </strong>
          </article>
        ))}
      </section>

      {/* ── Tabs ───────────────────────────────────────────────── */}
      <div className="pedidos-tabs" role="tablist" aria-label="Vistas de pedidos">
        {TABS.map((tab, i) => (
          <button
            key={tab}
            role="tab"
            aria-selected={activeTab === i}
            className={`pedidos-tab ${activeTab === i ? 'pedidos-tab--active' : ''}`}
            onClick={() => setActiveTab(i)}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* ══ Tab 0: Seguimiento en tiempo real ══════════════════════ */}
      {activeTab === 0 && (
        <div className="pedidos-body">

          {/* ── Columna izquierda: lista ───────────────────────── */}
          <aside className="pedidos-sidebar" aria-label="Lista de pedidos">

            <div className="pedidos-sidebar-search">
              <Search size={14} />
              <input
                type="search"
                placeholder="Buscar por ID o repartidor…"
                value={search}
                onChange={e => handleSearch(e.target.value)}
                aria-label="Buscar pedido"
              />
            </div>

            <div className="pedidos-list" role="listbox" aria-label="Pedidos">
              {ordersLoading && (
                <div className="table-state">
                  <span className="loading-spinner" /> Cargando…
                </div>
              )}
              {!ordersLoading && paginated.length === 0 && (
                <div className="table-state">Sin pedidos disponibles.</div>
              )}
              {paginated.map(order => (
                <div
                  key={order.order_id}
                  role="option"
                  aria-selected={selectedId === order.order_id}
                  className={`pedido-list-item ${selectedId === order.order_id ? 'pedido-list-item--active' : ''}`}
                  onClick={() => setSelectedId(order.order_id)}
                >
                  {/* Icono genérico de pedido — sin imagen de producto inventada */}
                  <span className="pedido-list-icon" aria-hidden="true">
                    <Package size={20} />
                  </span>
                  <div className="pedido-list-info">
                    <p className="pedido-list-id">
                      {order.order_id.slice(0, 8).toUpperCase()}
                    </p>
                    <StatusBadge status={order.state} />
                  </div>
                  <ChevronRight size={14} className="pedido-list-arrow" />
                </div>
              ))}
            </div>

            {/* Paginación */}
            <div className="pedidos-pagination" aria-label="Paginación de pedidos">
              <button
                className="page-btn"
                onClick={() => setPage(p => Math.max(1, p - 1))}
                disabled={page === 1}
                aria-label="Página anterior"
              >‹</button>

              {Array.from({ length: Math.min(totalPages, 5) }, (_, idx) => {
                const p = idx + 1;
                return (
                  <button
                    key={p}
                    className={`page-btn ${page === p ? 'page-btn--active' : ''}`}
                    onClick={() => setPage(p)}
                    aria-label={`Página ${p}`}
                    aria-current={page === p ? 'page' : undefined}
                  >{p}</button>
                );
              })}

              {totalPages > 5 && (
                <>
                  <span className="page-ellipsis">…</span>
                  <button
                    className={`page-btn ${page === totalPages ? 'page-btn--active' : ''}`}
                    onClick={() => setPage(totalPages)}
                    aria-label={`Última página ${totalPages}`}
                  >{totalPages}</button>
                </>
              )}

              <button
                className="page-btn"
                onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                aria-label="Página siguiente"
              >›</button>
            </div>

          </aside>

          {/* ── Columna derecha: seguimiento + historial ──────── */}
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 280px', gap: '16px' }}>
              <OrderTracker
                order={selectedOrder}
                loading={detailLoading}
                error={detailError}
                onCancel={setCancelOrderId}
              />
              <div className="pedidos-card">
                <OrderStateHistory order={selectedOrder} />
              </div>
            </div>
          </div>

        </div>
      )}

      {/* ══ Tab 1: Listado de pedidos ═══════════════════════════════ */}
      {activeTab === 1 && (
        <div className="pedidos-tab-content">
          <OrdersTable
            orders={filtered}
            loading={ordersLoading}
            error={ordersError}
            onViewDetail={id => setDetailModalId(id)}
            onCancel={id => setCancelOrderId(id)}
          />
        </div>
      )}

      {/* ══ Tab 2: Historial ════════════════════════════════════════ */}
      {activeTab === 2 && (
        <div className="pedidos-tab-content">
          <div style={{ marginBottom: '16px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '4px' }}>
              Historial de pedidos
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Todos los pedidos registrados en el sistema. Selecciona uno en
              la pestaña «Seguimiento» para ver su detalle.
            </p>
          </div>
          <OrdersTable
            orders={orders}
            loading={ordersLoading}
            error={ordersError}
            onViewDetail={id => { setDetailModalId(id); setActiveTab(0); setSelectedId(id); }}
            onCancel={id => setCancelOrderId(id)}
          />
        </div>
      )}

      {/* ── Modales existentes — sin modificar ─────────────────── */}
      {detailModalId && (
        <OrderDetailModal
          orderId={detailModalId}
          onClose={() => setDetailModalId(null)}
        />
      )}
      {cancelOrderId && (
        <CancelOrderModal
          orderId={cancelOrderId}
          onClose={() => setCancelOrderId(null)}
          onCancelled={handleCancelled}
        />
      )}

    </div>
  );
}
