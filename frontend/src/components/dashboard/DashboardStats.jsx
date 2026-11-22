import { ShoppingBag, CheckCircle2, Bike, Bell } from 'lucide-react';
import useDashboardStats from '../../hooks/useDashboardStats.js';

const CARDS = [
  {
    key:   'active_orders',
    label: 'Pedidos activos',
    icon:  ShoppingBag,
    color: 'stat-card--orange',
  },
  {
    key:   'delivered_orders',
    label: 'Pedidos entregados',
    icon:  CheckCircle2,
    color: 'stat-card--green',
  },
  {
    key:   'total_drivers',
    label: 'Repartidores activos',
    icon:  Bike,
    color: 'stat-card--blue',
  },
  {
    key:   'total_orders',
    label: 'Solicitudes pendientes',
    icon:  Bell,
    color: 'stat-card--red',
  },
];

export default function DashboardStats() {
  const { stats, loading, error } = useDashboardStats(15_000);

  return (
    <section className="stats-grid" aria-label="Métricas del sistema">
      {CARDS.map(({ key, label, icon: Icon, color }) => {
        const value = stats ? stats[key] : null;

        return (
          <article key={key} className={`stat-card ${color}`}>
            <div className="stat-top">
              <span className="stat-label">{label}</span>
              <span className="stat-icon"><Icon size={20} /></span>
            </div>

            <strong className="stat-value">
              {loading && value === null ? (
                <span className="stat-skeleton" aria-hidden="true" />
              ) : error ? (
                <span className="stat-error" title={error}>—</span>
              ) : (
                value ?? '—'
              )}
            </strong>

            {/* Mini sparkline placeholder — se conectará en la segunda entrega */}
            <div className="stat-trend" aria-hidden="true" />
          </article>
        );
      })}
    </section>
  );
}
