import { Bell, ChevronDown } from 'lucide-react';
import ConnectionIndicator from '../common/ConnectionIndicator.jsx';

/**
 * Header del panel administrativo.
 * Muestra: título + subtítulo | ConnectionIndicator | campana | usuario
 */
export default function DashboardHeader({ title, subtitle, user, wsStatus = 'closed' }) {
  return (
    <header className="dashboard-header">
      <div className="dashboard-header-left">
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>

      <div className="dashboard-header-right">
        <ConnectionIndicator status={wsStatus} />

        <button className="header-bell" aria-label="Notificaciones">
          <Bell size={18} />
          {/* Badge de notificaciones — segunda entrega */}
        </button>

        <button className="header-user" aria-label="Menú de usuario">
          <span className="header-user-avatar" aria-hidden="true">
            {(user?.name ?? 'A')[0].toUpperCase()}
          </span>
          {user?.name ?? 'Administrador'}
          <ChevronDown size={14} />
        </button>
      </div>
    </header>
  );
}
