import { Clock, RefreshCw } from 'lucide-react';

/**
 * Muestra el próximo repartidor a asignar y el turno actual.
 * Los datos vienen del backend — no se calcula nada aquí.
 */
export default function RoundRobinNextCard({ roundRobin, loading }) {
  if (loading) {
    return (
      <div className="rr-next-card rr-next-card--loading">
        <span className="loading-spinner" />
      </div>
    );
  }

  if (!roundRobin || !roundRobin.next_driver_id) {
    return (
      <div className="rr-next-card rr-next-card--empty">
        <Clock size={24} />
        <p>Sin asignación pendiente</p>
      </div>
    );
  }

  return (
    <div className="rr-next-card">
      <div className="rr-next-header">
        <Clock size={16} />
        <span>Siguiente asignación</span>
      </div>

      <div className="rr-next-body">
        <span className="rr-next-driver-avatar" aria-hidden="true">
          {roundRobin.next_driver_id[0]}
        </span>
        <div>
          <p className="rr-next-label">Se asignará a:</p>
          <strong className="rr-next-driver">{roundRobin.next_driver_id}</strong>
        </div>
      </div>

      <div className="rr-next-footer">
        <RefreshCw size={14} />
        <span>Turno Round Robin: {roundRobin.turn_label}</span>
      </div>
    </div>
  );
}
