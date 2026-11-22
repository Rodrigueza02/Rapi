import { Mail } from 'lucide-react';

/**
 * Panel de correos enviados.
 * Primera entrega: estado vacío real.
 * Tercera entrega: se conectará con notification-router.
 */
export default function EmailsPanel({ stats = null }) {
  return (
    <article className="dashboard-panel">
      <div className="panel-header">
        <h3><Mail size={18} /> Correos enviados</h3>
        <small className="panel-sub">Notificaciones y evidencias de entrega</small>
      </div>

      {!stats ? (
        <div className="panel-empty">
          <span className="panel-empty-icon">📧</span>
          <p>Sin datos de correos</p>
          <small>Se conectará con notification-router en la tercera entrega.</small>
        </div>
      ) : (
        <div className="emails-body">
          <strong className="emails-total">{stats.total}</strong>
          <ul className="emails-breakdown">
            <li>
              <span>✅ Confirmaciones de pago</span>
              <strong>{stats.confirmations}</strong>
            </li>
            <li>
              <span>📦 Evidencias de entrega</span>
              <strong>{stats.deliveries}</strong>
            </li>
            <li>
              <span>❌ Notificaciones de cancelación</span>
              <strong>{stats.cancellations}</strong>
            </li>
          </ul>
        </div>
      )}
    </article>
  );
}
