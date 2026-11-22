import { Bell, ArrowRight } from 'lucide-react';

/**
 * Panel de solicitudes de clientes.
 * Primera entrega: estado vacío real.
 * Segunda entrega: se conectará con el endpoint de solicitudes.
 */
export default function RequestsPanel({ requests = [] }) {
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
          <small>Las solicitudes de cancelación aparecerán aquí.</small>
        </div>
      ) : (
        <div className="requests-list">
          {requests.map(req => (
            <div key={req.id} className="request-item">
              <div className="request-info">
                <strong>{req.clientName}</strong>
                <span>Pedido {req.orderId}</span>
                <em>"{req.reason}"</em>
              </div>
              <div className="request-actions">
                <button className="btn-approve">✓ Aprobar</button>
                <button className="btn-reject">✕ Rechazar</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </article>
  );
}
