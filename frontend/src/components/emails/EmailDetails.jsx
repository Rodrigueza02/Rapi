import { X } from 'lucide-react';
import EmailStatusBadge from './EmailStatusBadge.jsx';

function Row({ label, value }) {
  if (!value) return null;
  return (
    <div className="email-detail-row">
      <span className="email-detail-label">{label}</span>
      <span className="email-detail-value">{value}</span>
    </div>
  );
}

function formatDate(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}

export default function EmailDetails({ email, onClose }) {
  if (!email) return null;
  return (
    <div className="email-detail-panel" role="complementary" aria-label="Detalle del correo">
      <div className="email-detail-header">
        <h3>Detalle del correo</h3>
        <button className="email-detail-close" onClick={onClose} aria-label="Cerrar">
          <X size={18} />
        </button>
      </div>
      <div className="email-detail-body">
        <div className="email-detail-status">
          <EmailStatusBadge status={email.status} />
        </div>
        <Row label="ID"           value={email.commandId} />
        <Row label="Destinatario" value={email.recipientEmail} />
        <Row label="Asunto"       value={email.subject} />
        <Row label="Tipo"         value={email.notificationType} />
        <Row label="Pedido"       value={email.orderId} />
        <Row label="Enviado"      value={formatDate(email.sentAt)} />
        <Row label="Creado"       value={formatDate(email.createdAt)} />
        {email.errorMessage && (
          <div className="email-detail-error">
            <span className="email-detail-label">Error</span>
            <p className="email-detail-error-msg">{email.errorMessage}</p>
          </div>
        )}
        <div className="email-detail-body-text">
          <span className="email-detail-label">Mensaje</span>
          <pre className="email-body-preview">{email.bodyText}</pre>
        </div>
      </div>
    </div>
  );
}
