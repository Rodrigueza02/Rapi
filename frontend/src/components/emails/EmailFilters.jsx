const STATUSES = [
  { value: '',               label: 'Todos'      },
  { value: 'PENDING',        label: 'Pendientes' },
  { value: 'SENDING',        label: 'Enviando'   },
  { value: 'SMTP_ACCEPTED',  label: 'Enviados'   },
  { value: 'FAILED',         label: 'Fallidos'   },
  { value: 'UNCERTAIN',      label: 'Inciertos'  },
];

export default function EmailFilters({ status, onChange }) {
  return (
    <div className="email-filters" role="toolbar" aria-label="Filtros de correos">
      {STATUSES.map(opt => (
        <button
          key={opt.value}
          className={`email-filter-btn ${status === opt.value ? 'email-filter-btn--active' : ''}`}
          onClick={() => onChange(opt.value)}
          aria-pressed={status === opt.value}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
}
