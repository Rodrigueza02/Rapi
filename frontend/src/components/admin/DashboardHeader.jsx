/**
 * DashboardHeader.jsx
 * Muestra el título, el indicador de conexión, campana y usuario.
 *
 * wsStatus refleja el estado real del WebSocket (desde AdminLayout).
 * El indicador distingue tres situaciones:
 *   - WebSocket activo ('open')      → tiempo real conectado
 *   - WebSocket no conectado         → datos disponibles via REST
 *   - Conectando ('connecting')      → intentando conectar
 */

import { Bell, ChevronDown } from 'lucide-react';
import ConnectionIndicator from '../common/ConnectionIndicator.jsx';

export default function DashboardHeader({ title, subtitle, user, wsStatus = 'closed' }) {
  const initials = (user?.name ?? 'A')[0].toUpperCase();

  return (
    <header className="dashboard-header">

      {/* ── Título ───────────────────────────────────────────── */}
      <div className="dashboard-header-left">
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>

      {/* ── Controles derechos ────────────────────────────────── */}
      <div className="dashboard-header-right">

        <ConnectionIndicator status={wsStatus} />

        <button className="header-bell" aria-label="Notificaciones">
          <Bell size={18} />
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
