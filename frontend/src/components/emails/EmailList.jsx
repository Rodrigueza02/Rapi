import { Mail } from 'lucide-react';
import EmailStatusBadge from './EmailStatusBadge.jsx';

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('es-CO', {
    dateStyle: 'short', timeStyle: 'short',
  });
}

export default function EmailList({ emails, loading, onSelect }) {
  if (loading) {
    return <div className="table-state"><span className="loading-spinner" /> Cargando…</div>;
  }
  if (!emails?.content?.length) {
    return (
      <div className="table-state">
        <Mail size={32} style={{ opacity: 0.3, marginBottom: 8 }} />
        <p>No hay correos registrados.</p>
      </div>
    );
  }
  return (
    <table className="email-table" role="grid" aria-label="Lista de correos">
      <thead>
        <tr>
          <th>Destinatario</th>
          <th>Asunto</th>
          <th>Estado</th>
          <th>Fecha</th>
        </tr>
      </thead>
      <tbody>
        {emails.content.map(email => (
          <tr
            key={email.commandId}
            className="email-table-row"
            onClick={() => onSelect(email)}
            role="row"
            tabIndex={0}
            onKeyDown={e => e.key === 'Enter' && onSelect(email)}
          >
            <td className="email-cell-recipient">{email.recipientEmail}</td>
            <td className="email-cell-subject">{email.subject}</td>
            <td><EmailStatusBadge status={email.status} /></td>
            <td className="email-cell-date">{formatDate(email.createdAt)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
