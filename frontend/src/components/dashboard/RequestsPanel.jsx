/**
 * RequestsPanel.jsx
 * SRP: muestra solicitudes de cancelación de clientes.
 *
 * FUENTE DE DATOS:
 *   Las solicitudes llegan del padre (AdminDashboard) vía props.
 *   No hay endpoint REST ni WebSocket dedicado para solicitudes pendientes.
 *   Cuando requests === [] el panel muestra el estado vacío real.
 *   Los botones de aprobación/rechazo ejecutan las callbacks del padre,
 *   que a su vez llaman al modal de cancelación real (CancelOrderModal).
 *
 * Props:
 *   requests  — array de { id, clientName, orderId, reason, timeAgo }
 *   onApprove — callback cuando el admin aprueba la cancelación
 *   onReject  — callback cuando el admin rechaza
 */

import { Bell, ArrowRight, XCircle, CheckCircle2 } from 'lucide-react';

export default function RequestsPanel({ requests = [], onApprove, onReject }) {
  return (
    <article className="dashboard-panel requests-panel">

      {/* ── Encabezado ───────────────────────────────────────────── */}
      <div className="panel-header">
        <div className="panel-title-wrap">
          <span className="panel-icon-wrap panel-icon--red">
            <Bell size={16} />
          </span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <h3 className="panel-title">Solicitudes de clientes</h3>
            {requests.length > 0 && (
              <span className="panel-badge">{requests.length}</span>
            )}
          </div>
        </div>
        <button className="text-link" aria-label="Ver todas las solicitudes">
          Ver todas <ArrowRight size={14} />
        </button>
      </div>

      {/* ── Estado vacío ─────────────────────────────────────────── */}
      {requests.length === 0 ? (
        <div className="requests-empty">
          <span className="requests-empty-icon">🔔</span>
          <p>Sin solicitudes pendientes</p>
          <small>
            Las solicitudes de cancelación aparecerán aquí
            cuando los clientes las envíen.
          </small>
        </div>
      ) : (
        /* ── Lista de solicitudes ─────────────────────────────── */
        <div className="requests-list">
          {requests.map(req => (
            <div key={req.id} className="request-card">

              {/* Cabecera de la tarjeta */}
              <div className="request-card-header">
                <div className="request-type-badge">
                  <XCircle size={13} />
                  Solicitud de cancelación
                </div>
                <span className="request-time">{req.timeAgo ?? ''}</span>
              </div>

              {/* Info del cliente */}
              <div className="request-client">
                <span className="request-avatar">
                  {(req.clientName?.[0] ?? '?').toUpperCase()}
                </span>
                <div>
                  <p className="request-client-name">
                    <strong>Cliente:</strong> {req.clientName ?? '—'}
                  </p>
                  <p className="request-order-id">
                    <strong>Pedido:</strong>{' '}
                    {req.orderId?.slice(0, 8).toUpperCase() ?? '—'}
                  </p>
                </div>
              </div>

              {/* Motivo */}
              {req.reason && (
                <p className="request-reason">"{req.reason}"</p>
              )}

              {/* Acciones */}
              <div className="request-actions">
                <button
                  className="btn-approve"
                  onClick={() => onApprove?.(req)}
                  aria-label="Aprobar cancelación"
                >
                  <CheckCircle2 size={14} />
                  Aprobar cancelación
                </button>
                <button
                  className="btn-reject"
                  onClick={() => onReject?.(req)}
                  aria-label="Rechazar solicitud"
                >
                  <XCircle size={14} />
                  Rechazar
                </button>
              </div>

            </div>
          ))}
        </div>
      )}

    </article>
  );
}
