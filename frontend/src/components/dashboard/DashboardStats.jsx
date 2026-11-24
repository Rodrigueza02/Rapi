/**
 * DashboardStats.jsx
 * Tarjetas globales + tarjetas por estado.
 *
 * Recibe los datos como props para que AdminDashboard sea la única
 * fuente de verdad y controle el refresco vía WebSocket.
 *
 * Props:
 *   stats   — objeto de DashboardStatsDto (active_orders, delivered_orders, …)
 *   orders  — array de OrderViewDto (se usa para el conteo por estado)
 *   loading — boolean
 */
import { ShoppingBag, CheckCircle2, Bike, Package } from 'lucide-react';
import { STATE_SEQUENCE, STATUS_LABEL, STATUS_COLOR } from '../../constants/orderStatus.js';

const STATE_ICON = {
  CREATED:    '📋',
  PREPARED:   '🍳',
  ASSIGNED:   '🛵',
  ON_THE_WAY: '🚀',
  ARRIVING:   '📍',
  DELIVERED:  '✅',
  CANCELLED:  '❌',
};

const TOP_CARDS = [
  { key: 'total_orders',     label: 'Pedidos totales', icon: Package,      color: 'red'    },
  { key: 'active_orders',    label: 'Pedidos activos', icon: ShoppingBag,  color: 'orange' },
  { key: 'delivered_orders', label: 'Entregados',      icon: CheckCircle2, color: 'green'  },
  { key: 'total_drivers',    label: 'Repartidores',    icon: Bike,         color: 'blue'   },
];

export default function DashboardStats({ stats, orders = [], loading = false }) {
  // Conteo por estado a partir de la lista completa de pedidos
  const countByState = Object.fromEntries(STATE_SEQUENCE.map(s => [s, 0]));
  for (const o of orders) {
    if (countByState[o.state] !== undefined) countByState[o.state]++;
  }

  return (
    <>
      {/* Tarjetas principales */}
      <section className="stats-grid" aria-label="Métricas globales">
        {TOP_CARDS.map(({ key, label, icon: Icon, color }) => (
          <article key={key} className={`stat-card stat-card--${color}`}>
            <div className="stat-top">
              <span className={`stat-icon stat-icon--${color}`}><Icon size={20} /></span>
            </div>
            <span className="stat-label">{label}</span>
            <strong className="stat-value">
              {loading
                ? <span className="stat-skeleton" aria-hidden="true" />
                : (stats?.[key] ?? '—')}
            </strong>
          </article>
        ))}
      </section>

      {/* Tarjetas por estado — orden cronológico estricto */}
      <section className="state-cards-grid" aria-label="Pedidos por estado">
        {STATE_SEQUENCE.map(state => (
          <article key={state} className={`state-card ${STATUS_COLOR[state]}`}>
            <span className="state-card-icon" aria-hidden="true">
              {STATE_ICON[state]}
            </span>
            <strong className="state-card-count">{countByState[state]}</strong>
            <span className="state-card-label">{STATUS_LABEL[state]}</span>
          </article>
        ))}
      </section>
    </>
  );
}
