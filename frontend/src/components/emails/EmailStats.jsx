import { Mail, CheckCircle2, XCircle, Clock, AlertCircle, Send } from 'lucide-react';

const CARDS = [
  { key: 'total',    label: 'Total',       icon: Mail,         color: 'orange' },
  { key: 'accepted', label: 'Enviados',    icon: CheckCircle2, color: 'green'  },
  { key: 'pending',  label: 'Pendientes',  icon: Clock,        color: 'blue'   },
  { key: 'sending',  label: 'Enviando',    icon: Send,         color: 'blue'   },
  { key: 'failed',   label: 'Fallidos',    icon: XCircle,      color: 'red'    },
  { key: 'uncertain',label: 'Inciertos',   icon: AlertCircle,  color: 'yellow' },
];

export default function EmailStats({ stats, loading }) {
  return (
    <section className="email-stats" aria-label="Estadísticas de correos">
      {CARDS.map(({ key, label, icon: Icon, color }) => (
        <article key={key} className={`stat-card stat-card--${color}`}>
          <div className="stat-top">
            <span className={`stat-icon stat-icon--${color}`}><Icon size={20} /></span>
          </div>
          <span className="stat-label">{label}</span>
          <strong className="stat-value">
            {loading ? <span className="stat-skeleton" /> : (stats?.[key] ?? 0)}
          </strong>
        </article>
      ))}
    </section>
  );
}
