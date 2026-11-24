/**
 * OrderTracker.jsx
 * Vista central de seguimiento de un pedido seleccionado.
 *
 * ESTADOS REALES DEL BACKEND (OrderViewState.java):
 *   ASSIGNED  → pedido asignado a un repartidor
 *   ARRIVING  → repartidor en camino al destino
 *   DELIVERED → pedido entregado
 *
 * El stepper refleja exactamente estos tres estados.
 * No se muestra "Llegando" como etapa independiente porque
 * el backend no expone ese estado en el dashboard-aggregator.
 *
 * DATOS DISPONIBLES POR PEDIDO (OrderViewDto.java):
 *   order_id     → real ✅
 *   driver_id    → real ✅
 *   state        → real ✅
 *   last_updated → real ✅
 *   customer_id, delivery_address, total_amount, products
 *                → NO disponibles en este endpoint ❌
 */

import {
  Truck, CheckCircle2, MapPin, XCircle, Clock, CreditCard,
} from 'lucide-react';
import StatusBadge     from '../common/StatusBadge.jsx';
import OrderMapDemo    from './OrderMapDemo.jsx';
import OrderStateHistory from './OrderStateHistory.jsx';
import { formatTime }  from '../../utils/formatDate.js';

/* Pasos del stepper — exactamente los estados del backend */
const STEPPER_STEPS = [
  { state: 'ASSIGNED',  label: 'Asignado',   icon: Truck        },
  { state: 'ARRIVING',  label: 'En camino',   icon: MapPin       },
  { state: 'DELIVERED', label: 'Entregado',   icon: CheckCircle2 },
];

const STATE_ORDER = { ASSIGNED: 0, ARRIVING: 1, DELIVERED: 2 };

export default function OrderTracker({ order, loading, error, onCancel }) {

  if (loading) {
    return (
      <div className="pedidos-card pedidos-empty">
        <span className="loading-spinner" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="pedidos-card pedidos-empty">
        <XCircle size={36} style={{ color: '#EF4444' }} />
        <p>Error al cargar el pedido</p>
        <small>{error}</small>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="pedidos-card pedidos-empty">
        <span className="pedidos-empty-icon">📦</span>
        <p>Selecciona un pedido</p>
        <small>Haz clic en cualquier pedido de la lista para ver su seguimiento.</small>
      </div>
    );
  }

  const currentStep  = STATE_ORDER[order.state] ?? -1;
  const shortId      = order.order_id?.slice(0, 8).toUpperCase() ?? '—';
  const driverLabel  = order.driver_id ?? '—';
  const driverInit   = order.driver_id
    ? order.driver_id.replace(/[^a-zA-Z0-9]/g, '').slice(0, 2).toUpperCase()
    : '?';

  return (
    <div className="pedidos-detail">

      {/* ── Tarjeta principal ──────────────────────────────────── */}
      <div className="pedidos-card">

        {/* Cabecera */}
        <div className="order-detail-header">
          <div className="order-detail-id-block">
            <span className="order-detail-id">Pedido {shortId}</span>
            <span className="order-detail-client" style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
              Última actualización: {formatTime(order.last_updated)}
            </span>
          </div>
          <div>
            <StatusBadge status={order.state} />
          </div>
        </div>

        {/* Stepper — solo estados reales del backend */}
        <div className="order-stepper" role="list" aria-label="Progreso del pedido">
          {STEPPER_STEPS.map((step, i) => {
            const isDone   = i < currentStep;
            const isActive = i === currentStep;
            const status   = isDone ? 'done' : isActive ? 'active' : 'pending';
            const Icon     = step.icon;

            return (
              <div
                key={step.state}
                className={`stepper-step stepper-step--${status}`}
                role="listitem"
                aria-current={isActive ? 'step' : undefined}
              >
                <div className="stepper-dot">
                  <Icon size={14} />
                </div>
                <span className="stepper-label">{step.label}</span>
                <span className="stepper-time">
                  {isActive ? formatTime(order.last_updated) : '—'}
                </span>
              </div>
            );
          })}
        </div>

        {/* Repartidor asignado */}
        {order.driver_id ? (
          <div className="order-driver-row">
            <div className="order-driver-info">
              <div className="order-driver-avatar" aria-hidden="true">{driverInit}</div>
              <div>
                <p className="order-driver-label">Repartidor asignado</p>
                <p className="order-driver-name">{driverLabel}</p>
              </div>
            </div>
            {/* Botones indicativos — sin integración real disponible */}
            <div className="order-driver-actions">
              <span
                className="btn-action-outline"
                title="Contacto no disponible en este endpoint"
                aria-label="Contacto no disponible"
              >
                <Clock size={13} /> Contactar
              </span>
              <span
                className="btn-action-outline"
                title="Ubicación GPS no disponible"
                aria-label="GPS no disponible"
              >
                <MapPin size={13} /> Ubicación
              </span>
            </div>
          </div>
        ) : (
          <div className="order-driver-row">
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Sin repartidor asignado.
            </span>
          </div>
        )}

      </div>

      {/* ── Mapa demostrativo ───────────────────────────────────── */}
      <div className="pedidos-card" style={{ padding: '14px' }}>
        <OrderMapDemo state={order.state} />
      </div>

      {/* ── Detalles del pedido ─────────────────────────────────── */}
      <div className="order-details-grid">

        <div className="pedidos-card order-details-block">
          <div className="order-details-title">
            <Truck size={15} />
            Detalles del pedido
          </div>

          <div className="detail-row">
            <CreditCard size={13} />
            <span>
              <strong>Repartidor: </strong>{driverLabel}
            </span>
          </div>
          <div className="detail-row">
            <Clock size={13} />
            <span>
              <strong>Última actualización: </strong>
              {formatTime(order.last_updated)}
            </span>
          </div>

          {/* Acción cancelar — solo si el pedido no está entregado */}
          {order.state !== 'DELIVERED' && (
            <button
              className="btn-danger"
              style={{ marginTop: '12px', width: '100%' }}
              onClick={() => onCancel?.(order.order_id)}
              aria-label={`Cancelar pedido ${shortId}`}
            >
              <XCircle size={14} />
              Cancelar pedido
            </button>
          )}
        </div>

        {/* Estado del pedido */}
        <div className="pedidos-card order-eta-block">
          {order.state === 'DELIVERED' ? (
            <>
              <CheckCircle2 size={36} style={{ color: '#10B981', margin: '8px 0' }} />
              <p className="order-eta-label">Pedido entregado</p>
              <span className="order-eta-na"
                style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '6px', textAlign: 'center' }}>
                Actualizado: {formatTime(order.last_updated)}
              </span>
            </>
          ) : order.state === 'ARRIVING' ? (
            <>
              <MapPin size={32} style={{ color: 'var(--orange)', margin: '8px 0' }} />
              <p className="order-eta-label">En camino</p>
              <span className="order-eta-na">Repartidor en ruta</span>
              <small style={{ fontSize: '10px', color: 'var(--text-muted)', marginTop: '4px', textAlign: 'center' }}>
                Tiempo estimado no disponible
              </small>
            </>
          ) : order.state === 'ASSIGNED' ? (
            <>
              <Truck size={32} style={{ color: '#3B82F6', margin: '8px 0' }} />
              <p className="order-eta-label">Asignado</p>
              <span className="order-eta-na">Repartidor asignado</span>
            </>
          ) : (
            <span className="order-eta-na">—</span>
          )}
        </div>

      </div>

    </div>
  );
}
