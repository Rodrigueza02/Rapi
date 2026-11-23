/**
 * DashboardStats.jsx
 * Muestra las cuatro métricas superiores del dashboard.
 *
 * Fuente real: useDashboardStats → GET /api/dashboard/stats
 * Campos reales del endpoint (DashboardStatsDto.java):
 *   active_orders    → pedidos en ASSIGNED o ARRIVING
 *   delivered_orders → pedidos en DELIVERED
 *   total_orders     → todos los pedidos conocidos
 *   total_drivers    → repartidores únicos vistos en esta sesión del servicio
 *
 * Los porcentajes de tendencia (▲ ▼) no tienen respaldo histórico real.
 * Se eliminaron para no presentar datos ficticios como si fueran reales.
 */

import { ShoppingBag, CheckCircle2, Bike, Package } from 'lucide-react';
import useDashboardStats from '../../hooks/useDashboardStats.js';
import SparkLine from './SparkLine.jsx';

const CARDS = [
  {
    key:   'active_orders',
    label: 'Pedidos activos',
    icon:  ShoppingBag,
    color: 'orange',
  },
  {
    key:   'delivered_orders',
    label: 'Pedidos entregados',
    icon:  CheckCircle2,
    color: 'green',
  },
  {
    key:   'total_drivers',
    label: 'Repartidores activos',
    icon:  Bike,
    color: 'blue',
  },
  {
    key:   'total_orders',
    label: 'Total de pedidos',
    icon:  Package,
    color: 'red',
  },
];

export default function DashboardStats() {
  const { stats, loading } = useDashboardStats(15_000);

  return (
    <section className="stats-grid" aria-label="Métricas del sistema">
      {CARDS.map(({ key, label, icon: Icon, color }) => {
        const value = stats?.[key] ?? null;

        return (
          <article key={key} className={`stat-card stat-card--${color}`}>

            <div className="stat-top">
              <span className={`stat-icon stat-icon--${color}`}>
                <Icon size={20} />
              </span>
            </div>

            <span className="stat-label">{label}</span>

            <strong className="stat-value">
              {loading && value === null
                ? <span className="stat-skeleton" aria-hidden="true" />
                : (value ?? '—')}
            </strong>

            <SparkLine color={color} />

          </article>
        );
      })}
    </section>
  );
}
