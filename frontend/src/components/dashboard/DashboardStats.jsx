import { ShoppingBag, CheckCircle2, Bike, Bell } from 'lucide-react';
import useDashboardStats from '../../hooks/useDashboardStats.js';
import SparkLine from './SparkLine.jsx';

const CARDS = [
  {
    key:    'active_orders',
    label:  'Pedidos activos',
    icon:   ShoppingBag,
    color:  'orange',
    trend:  '+12%',
    trendUp: true,
  },
  {
    key:    'delivered_orders',
    label:  'Pedidos entregados',
    icon:   CheckCircle2,
    color:  'green',
    trend:  '+8%',
    trendUp: true,
  },
  {
    key:    'total_drivers',
    label:  'Repartidores activos',
    icon:   Bike,
    color:  'blue',
    trend:  null,
    trendUp: true,
  },
  {
    key:    'total_orders',
    label:  'Solicitudes pendientes',
    icon:   Bell,
    color:  'red',
    trend:  null,
    trendUp: false,
  },
];

export default function DashboardStats() {
  const { stats, loading } = useDashboardStats(15_000);

  return (
    <section className="stats-grid" aria-label="Métricas del sistema">
      {CARDS.map(({ key, label, icon: Icon, color, trend, trendUp }) => {
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
            {trend && (
              <span className={`stat-trend-label ${trendUp ? 'trend--up' : 'trend--down'}`}>
                {trendUp ? '▲' : '▼'} {trend}
              </span>
            )}
            <SparkLine color={color} />
          </article>
        );
      })}
    </section>
  );
}
