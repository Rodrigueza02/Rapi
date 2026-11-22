import { CreditCard } from 'lucide-react';

/**
 * Panel de pagos simulados.
 * Primera entrega: estado vacío real.
 * Tercera entrega: se conectará con el servicio de pagos.
 */
export default function PaymentsPanel({ stats = null }) {
  return (
    <article className="dashboard-panel">
      <div className="panel-header">
        <h3><CreditCard size={18} /> Pagos simulados</h3>
        <small className="panel-sub">Resumen del día</small>
      </div>

      {!stats ? (
        <div className="panel-empty">
          <span className="panel-empty-icon">💳</span>
          <p>Sin datos de pagos</p>
          <small>Se conectará en la tercera entrega.</small>
        </div>
      ) : (
        <div className="payments-body">
          <strong className="payments-total">{stats.total}</strong>
          <ul className="payments-breakdown">
            <li className="pay-approved">
              <span>✅ Pagos aprobados</span>
              <strong>{stats.approved}</strong>
            </li>
            <li className="pay-pending">
              <span>🕐 En validación</span>
              <strong>{stats.pending}</strong>
            </li>
            <li className="pay-rejected">
              <span>❌ Rechazados</span>
              <strong>{stats.rejected}</strong>
            </li>
          </ul>
        </div>
      )}
    </article>
  );
}
