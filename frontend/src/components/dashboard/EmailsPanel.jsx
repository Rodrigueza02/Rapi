/**
 * EmailsPanel.jsx
 * SRP: muestra el resumen de correos enviados por el notification-router.
 *
 * FUENTE DE DATOS:
 *   El notification-router y push-worker existen en el backend pero no
 *   exponen un endpoint REST de historial consultable desde el frontend.
 *   Este panel utiliza datos DEMOSTRATIVOS para la exposición,
 *   claramente identificados con la etiqueta "Simulado".
 *   Cuando el prop `stats` llegue con datos reales, los mostrará.
 *
 * Props:
 *   stats — { total, confirmations, deliveries, cancellations } | null
 *           Si es null se muestran los datos demostrativos.
 */

import { Mail, CheckCircle2, Package, XCircle, TrendingUp } from 'lucide-react';

/* Datos demostrativos — se muestran sólo cuando stats === null */
const DEMO = {
  total:         186,
  trend:         '+100%',
  confirmations: 134,
  deliveries:    42,
  cancellations: 10,
};

export default function EmailsPanel({ stats = null }) {
  const data   = stats ?? DEMO;
  const isDemo = stats === null;

  return (
    <article className="dashboard-panel emails-panel">

      {/* ── Encabezado ───────────────────────────────────────────── */}
      <div className="panel-header">
        <div className="panel-title-wrap">
          <span className="panel-icon-wrap panel-icon--blue">
            <Mail size={16} />
          </span>
          <div>
            <h3 className="panel-title">Correos enviados</h3>
            <p className="panel-sub">Notificaciones y evidencias de entrega</p>
          </div>
        </div>
        {isDemo && (
          <span className="demo-badge">Simulado</span>
        )}
      </div>

      {/* ── Total principal ──────────────────────────────────────── */}
      <div className="payments-total-row">
        <strong className="payments-total">{data.total}</strong>
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
          <span className="breakdown-label">Confirmaciones de pago</span>
          <strong className="breakdown-value">{data.confirmations}</strong>
        </li>
        <li>
          <span className="breakdown-icon breakdown-icon--blue">
            <Package size={14} />
          </span>
          <span className="breakdown-label">Evidencias de entrega</span>
          <strong className="breakdown-value">{data.deliveries}</strong>
        </li>
        <li>
          <span className="breakdown-icon breakdown-icon--red">
            <XCircle size={14} />
          </span>
          <span className="breakdown-label">Notificaciones de cancelación</span>
          <strong className="breakdown-value">{data.cancellations}</strong>
        </li>
      </ul>

    </article>
  );
}
