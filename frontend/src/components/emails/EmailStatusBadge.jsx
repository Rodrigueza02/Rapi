const STATUS_CONFIG = {
  PENDING:        { label: 'Pendiente',  cls: 'badge--pending'  },
  SENDING:        { label: 'Enviando',   cls: 'badge--sending'  },
  SMTP_ACCEPTED:  { label: 'Enviado',    cls: 'badge--sent'     },
  FAILED:         { label: 'Fallido',    cls: 'badge--failed'   },
  UNCERTAIN:      { label: 'Incierto',   cls: 'badge--uncertain'},
};

export default function EmailStatusBadge({ status }) {
  const cfg = STATUS_CONFIG[status] ?? { label: status, cls: 'badge--pending' };
  return <span className={`email-badge ${cfg.cls}`}>{cfg.label}</span>;
}
