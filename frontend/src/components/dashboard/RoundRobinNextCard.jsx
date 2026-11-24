/**
 * RoundRobinNextCard.jsx
 * Muestra la siguiente asignación Round Robin.
 *
 * FUENTE: /api/dashboard/round-robin (datos reales del backend).
 *
 * Estado vacío: el anillo Round Robin es in-memory en el backend.
 * Al reiniciar el contenedor, se pierde el estado. Se reconstruye
 * solo cuando llegan nuevos eventos DRIVER_ASSIGNED vía Kafka.
 */

import { Clock, RefreshCw } from 'lucide-react';

export default function RoundRobinNextCard({ roundRobin, loading }) {

  if (loading) {
    return (
      <div className="rr-next-card rr-next-card--loading">
        <span className="loading-spinner" aria-label="Cargando asignación" />
      </div>
    );
  }

  if (!roundRobin || !roundRobin.next_driver_id) {
    return (
      <div className="rr-next-card rr-next-card--empty">
        <Clock size={28} className="rr-next-empty-icon" />
        <p>Anillo sin datos</p>
        <small>
          El anillo Round Robin se activa al recibir nuevos pedidos.
          Crea un pedido para ver la siguiente asignación.
        </small>
      </div>
    );
  }

  const { next_driver_id, turn_label } = roundRobin;
  const initials = next_driver_id.replace(/[^a-zA-Z0-9]/g, '').slice(0, 2).toUpperCase();

  return (
    <div className="rr-next-card">

      <div className="rr-next-header">
        <Clock size={15} />
        <span>Siguiente asignación</span>
      </div>

      <div className="rr-next-body">
        <div className="rr-next-avatar" aria-hidden="true">
          {initials}
        </div>
        <div className="rr-next-info">
          <p className="rr-next-label">Se asignará a:</p>
          <strong className="rr-next-driver">{next_driver_id}</strong>
        </div>
      </div>

      <div className="rr-next-footer">
        <RefreshCw size={13} />
        <span>Turno Round Robin: <strong>{turn_label}</strong></span>
      </div>

    </div>
  );
}
