import { Bell, ChevronDown } from 'lucide-react';
import ConnectionIndicator from '../common/ConnectionIndicator.jsx';

export default function DashboardHeader({ title, subtitle, user, wsStatus = 'closed' }) {
  const initials = (user?.name ?? 'A')[0].toUpperCase();

  return (
    <header className="dashboard-header">

      {/* ── Título ───────────────────────────────────────── */}
      <div className="dashboard-header-left">
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>

      {/* ── Controles derecha ─────────────────────────────── */}
      <div className="dashboard-header-right">

        <ConnectionIndicator status={wsStatus} />

        <button className="header-bell" aria-label="Notificaciones">
          <Bell size={18} />
          <span className="header-bell-badge">3</span>
        </button>

        <button className="header-user" aria-label="Menú de usuario">
          <span className="header-user-avatar" aria-hidden="true">
            {initials}
          </span>
          <span>{user?.name ?? 'Administrador'}</span>
          <ChevronDown size={14} />
        </button>

      </div>
    </header>
  );
}
