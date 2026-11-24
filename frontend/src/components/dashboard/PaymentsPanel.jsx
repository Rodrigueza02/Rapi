/**
 * PaymentsPanel.jsx
 * SRP: muestra el resumen de pagos simulados del día.
 *
 * FUENTE DE DATOS:
 *   El backend no expone un endpoint de pagos todavía.
 *   Este panel utiliza datos DEMOSTRATIVOS para la exposición,
 *   claramente identificados con la etiqueta "Simulado".
 *   Cuando el prop `stats` llegue con datos reales, los mostrará.
 *
 * Props:
 *   stats — { totalAmount, approved, pending, rejected } | null
 *           Si es null se muestran los datos demostrativos.
 */

import { CreditCard, CheckCircle2, Clock, XCircle, TrendingUp } from 'lucide-react';

/* Datos demostrativos — se muestran sólo cuando stats === null */
const DEMO = {
  totalAmount: '$ 5.430.000',
  trend:       '+15%',
  approved:    186,
  pending:     4,
  rejected:    2,
};

export default function PaymentsPanel({ stats = null }) {
  const data = stats ?? DEMO;
  const isDemo = stats === null;

  return (
    <article className="dashboard-panel payments-panel">

      {/* ── Encabezado ───────────────────────────────────────────── */}
      <div className="panel-header">
        <div className="panel-title-wrap">
          <span className="panel-icon-wrap panel-icon--orange">
            <CreditCard size={16} />
          </span>
          <div>
            <h3 className="panel-title">Pagos simulados</h3>
            <p className="panel-sub">Resumen del día</p>
          </div>
        </div>
        {isDemo && (
          <span className="demo-badge">Simulado</span>
        )}
      </div>

      {/* ── Total principal ──────────────────────────────────────── */}
      <div className="payments-total-row">
        <strong className="payments-total">{data.totalAmount}</strong>
        <span className="payments-trend trend--up">
          <TrendingUp size={13} />
          {data.trend}
        </span>
      </div>

      {/* ── Desglose ─────────────────────────────────────────────── */}
      <ul className="payments-breakdown">
        <li>
          <span className="breakdown-icon breakdown-icon--green">
            <CheckCircle2 size={14} />
          </span>
          <span className="breakdown-label">Pagos aprobados</span>
          <strong className="breakdown-value">{data.approved}</strong>
        </li>
        <li>
          <span className="breakdown-icon breakdown-icon--yellow">
            <Clock size={14} />
          </span>
          <span className="breakdown-label">En validación</span>
          <strong className="breakdown-value">{data.pending}</strong>
        </li>
        <li>
          <span className="breakdown-icon breakdown-icon--red">
            <XCircle size={14} />
          </span>
          <span className="breakdown-label">Rechazados</span>
          <strong className="breakdown-value">{data.rejected}</strong>
        </li>
      </ul>

    </article>
  );
}
