/**
 * OrderStateHistory.jsx
 * Muestra el progreso de estados de un pedido.
 *
 * IMPORTANTE — distinción entre progreso y historial:
 *
 *   PROGRESO VISUAL: se infiere del estado actual del pedido.
 *   Los pasos completados se marcan como tales, pero NO se
 *   muestran timestamps inventados para ellos.
 *
 *   HISTORIAL REAL: el backend (OrderViewDto) solo devuelve
 *   el estado ACTUAL y last_updated. No hay endpoint de historial
 *   de transiciones por pedido. Por eso solo el paso activo
 *   muestra timestamp real.
 *
 *   Esta distinción es clara para el usuario a través de la nota
 *   informativa al pie del componente.
 */

import { CheckCircle2, Truck, MapPin, Clock } from 'lucide-react';
import { formatTime }   from '../../utils/formatDate.js';
import { STATUS_LABEL } from '../../constants/orderStatus.js';

/* Estados en orden de progresión — exactamente los del backend */
const STEPS = [
  {
    state:  'ASSIGNED',
    label:  'Pedido asignado',
    desc:   'Se asignó un repartidor al pedido.',
    icon:   Truck,
  },
  {
    state:  'ARRIVING',
    label:  'En camino',
    desc:   'El repartidor se dirige al destino de entrega.',
    icon:   MapPin,
  },
  {
    state:  'DELIVERED',
    label:  'Entregado',
    desc:   'El pedido fue entregado correctamente.',
    icon:   CheckCircle2,
  },
];

const STATE_ORDER = { ASSIGNED: 0, ARRIVING: 1, DELIVERED: 2 };

export default function OrderStateHistory({ order }) {
  if (!order) {
    return (
      <div className="state-history">
        <div className="state-history-title">
          <Clock size={16} />
          Historial de estados
        </div>
        <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
          Selecciona un pedido para ver su progreso.
        </p>
      </div>
    );
  }

  const currentOrder = STATE_ORDER[order.state] ?? -1;

  return (
    <div className="state-history">
      <div className="state-history-title">
        <CheckCircle2 size={16} />
        Progreso del pedido
      </div>

      <ul className="history-list" aria-label="Progreso del pedido">
        {STEPS.map((step, i) => {
          const isDone    = i < currentOrder;
          const isActive  = step.state === order.state;
          const isPending = i > currentOrder;

          const statusClass = isDone    ? 'history-item--done'
            : isActive                  ? 'history-item--active'
            : 'history-item--pending';

          const Icon = step.icon;

          return (
            <li key={step.state} className={`history-item ${statusClass}`}>
              <span className="history-dot" aria-hidden="true">
                <Icon size={13} />
              </span>
              <div className="history-content">
                <p className="history-state">{step.label}</p>
                <p className="history-time">
                  {/* Solo el paso activo tiene timestamp real */}
                  {isActive
                    ? formatTime(order.last_updated)
                    : isDone
                      ? 'Completado'
                      : 'Pendiente'}
                </p>
                <p className="history-desc" style={{ opacity: isPending ? 0.5 : 1 }}>
                  {step.desc}
                </p>
              </div>
            </li>
          );
        })}
      </ul>

      {/* Nota de transparencia — el usuario sabe exactamente qué es real */}
      <p style={{
        fontSize: '10px',
        color: 'var(--text-muted)',
        marginTop: '12px',
        fontStyle: 'italic',
        lineHeight: '1.5',
        borderTop: '1px solid #F2E9E2',
        paddingTop: '8px',
      }}>
        Solo el estado actual proviene del backend en tiempo real.
        Los timestamps de etapas anteriores no están disponibles en este endpoint.
      </p>
    </div>
  );
}
