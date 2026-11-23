/**
 * RequestsPanel.jsx
 * SRP: única responsabilidad — mostrar y gestionar solicitudes de cancelación.
 * Las solicitudes llegan del padre (AdminDashboard) vía props.
 */

import { Bell, ArrowRight } from 'lucide-react';

export default function RequestsPanel({ requests = [], onApprove, onReject }) {
  return (
    <article className="dashboard-panel">
      <div className="panel-header">
        <h3>
          <Bell size={18} />
          Solicitudes de clientes
          {requests.length > 0 && (
            <span className="panel-badge">{requests.length}</span>
          )}
        </h3>
        <button className="text-link">
          Ver todas <ArrowRight size={14} />
        </button>
      </div>

      {requests.length === 0 ? (
        <div className="panel-empty">
          <span className="panel-empty-icon">🔔</span>
          <p>Sin solicitudes pendientes</p>
          <small>Las solicitudes de cancelación de clientes aparecerán aquí.</small>
        </div>
      ) : (
        <div className="requests-list">
          {requests.map(req => (
            <div key={req.id} className="request-item">
              <div className="request-info">
                <strong>{req.clientName}</strong>
                <span>Pedido {req.orderId?.slice(0, 8).toUpperCase()}</span>
                {req.reason && <em>"{req.reason}"</em>}
              </div>
              <div className="request-actions">
                <button className="btn-approve" onClick={() => onApprove?.(req)}>
                  ✓ Aprobar cancelación
                </button>
                <button className="btn-reject" onClick={() => onReject?.(req)}>
                  ✕ Rechazar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </article>
  );
}
