/**
 * OrderStateHistory.jsx — línea de tiempo cronológica real de 6 estados.
 * Muestra timestamp real solo para el estado activo (last_updated).
 * Para estados completados muestra "Completado" sin timestamp inventado.
 */
import { CheckCircle2, Truck, MapPin, Clock, ChefHat, Package } from 'lucide-react';
import { formatTime } from '../../utils/formatDate.js';

const STEPS = [
  { state: 'CREATED',    label: 'Creado',           icon: Package,     desc: 'Pedido recibido.' },
  { state: 'PREPARED',   label: 'En preparación',   icon: ChefHat,     desc: 'El restaurante está preparando el pedido.' },
  { state: 'ASSIGNED',   label: 'Asignado',         icon: Truck,       desc: 'Repartidor asignado.' },
  { state: 'ON_THE_WAY', label: 'En camino',        icon: Truck,       desc: 'El repartidor se dirige al destino.' },
  { state: 'ARRIVING',   label: 'Llegando',         icon: MapPin,      desc: 'El repartidor está llegando.' },
  { state: 'DELIVERED',  label: 'Entregado',        icon: CheckCircle2, desc: 'Pedido entregado correctamente.' },
];

const STATE_ORDER = Object.fromEntries(STEPS.map((s, i) => [s.state, i]));

export default function OrderStateHistory({ order }) {
  if (!order) {
    return (
      <div className="state-history">
        <div className="state-history-title"><Clock size={16} /> Historial de estados</div>
        <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Selecciona un pedido.</p>
      </div>
    );
  }

  const isCancelled   = order.state === 'CANCELLED';
  const currentIndex  = STATE_ORDER[order.state] ?? -1;

  return (
    <div className="state-history">
      <div className="state-history-title">
        <CheckCircle2 size={16} /> Progreso del pedido
      </div>

      {isCancelled && (
        <div className="state-cancelled-banner">❌ Pedido cancelado</div>
      )}

      <ul className="history-list" aria-label="Progreso del pedido">
        {STEPS.map((step, i) => {
          const isDone    = !isCancelled && i < currentIndex;
          const isActive  = !isCancelled && step.state === order.state;
          const isPending = isCancelled || i > currentIndex;
          const cls = isDone ? 'history-item--done' : isActive ? 'history-item--active' : 'history-item--pending';
          const Icon = step.icon;

          return (
            <li key={step.state} className={`history-item ${cls}`}>
              <span className="history-dot" aria-hidden="true"><Icon size={13} /></span>
              <div className="history-content">
                <p className="history-state">{step.label}</p>
                <p className="history-time">
                  {isActive ? formatTime(order.last_updated) : isDone ? 'Completado' : 'Pendiente'}
                </p>
                <p className="history-desc" style={{ opacity: isPending ? 0.5 : 1 }}>{step.desc}</p>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
