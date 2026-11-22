import { ShoppingBag, Bike, CheckCircle2, Clock3, Activity, Route } from 'lucide-react';
import StatCard from '../../components/admin/StatCard';

const STATS = [
  ['Pedidos totales', '—', ShoppingBag],
  ['En camino',       '—', Bike],
  ['Entregados',      '—', CheckCircle2],
  ['Pendientes',      '—', Clock3],
];

export default function AdminDashboard() {
  return (
    <>
      <div className="section-heading">
        <div>
          <h2>Resumen operativo</h2>
          <p>Información disponible cuando conectemos los endpoints de métricas.</p>
        </div>
        <span className="status-pill">Integración pendiente</span>
      </div>

      <section className="stats-grid">
        {STATS.map(([label, value, icon]) => (
          <StatCard
            key={label}
            label={label}
            value={value}
            icon={icon}
            note="Sin datos conectados"
          />
        ))}
      </section>

      <section className="panel-grid">
        <article className="panel">
          <h3><Activity size={20} /> Actividad de pedidos</h3>
          <p>Las estadísticas se cargarán desde el backend de dashboard.</p>
          <div className="panel-placeholder">Sin datos operativos disponibles</div>
        </article>
        <article className="panel">
          <h3><Route size={20} /> Asignación Round Robin</h3>
          <p>Se visualizará la asignación real de repartidores.</p>
          <div className="panel-placeholder">Esperando endpoint de asignaciones</div>
        </article>
      </section>
    </>
  );
}
