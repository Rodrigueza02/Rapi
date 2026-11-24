/**
 * AdminLayout.jsx
 * Gestiona el estado del WebSocket y lo propaga al DashboardHeader.
 *
 * El hook useWebSocket abre UNA conexión ws://localhost:8086/ws y devuelve
 * el estado real de la conexión. El layout lo pasa al header para que
 * el indicador refleje la situación real (no siempre 'closed').
 *
 * Las páginas hijas (AdminDashboard, PedidosPage) tienen sus propias
 * llamadas a useWebSocket para el reload de datos — eso no cambia.
 */

import { useCallback } from 'react';
import { Outlet, useLocation } from 'react-router-dom';

import AdminSidebar    from '../components/admin/AdminSidebar.jsx';
import DashboardHeader from '../components/admin/DashboardHeader.jsx';
import useWebSocket    from '../hooks/useWebSocket.js';

const MODULES = {
  pedidos: {
    title:    'Gestión de Pedidos',
    subtitle: 'Seguimiento y control de pedidos en tiempo real',
  },
  solicitudes: {
    title:    'Solicitudes de repartidores',
    subtitle: 'Gestiona, revisa y aprueba las solicitudes de nuevos repartidores.',
  },
  repartidores: {
    title:    'Repartidores',
    subtitle: 'Gestión de la flota de repartidores y su disponibilidad en tiempo real',
  },
  pagos: {
    title:    'Pagos',
    subtitle: 'Gestión de cobros, transacciones y reembolsos',
  },
  correos: {
    title:    'Correos enviados',
    subtitle: 'Historial de notificaciones y comunicaciones',
  },
};

export default function AdminLayout({ session, onLogout }) {
  const location = useLocation();

  /* ── Estado real del WebSocket ────────────────────────────────
     Esta instancia solo sirve para actualizar el indicador de
     conexión en el header. No dispara reloads de datos —
     cada página hijo tiene su propio useWebSocket para eso.       */
  const noop = useCallback(() => {}, []);
  const { wsStatus } = useWebSocket(noop);

  /* ── Título y subtítulo según la ruta ─────────────────────── */
  const moduleKey     = location.pathname.split('/')[2];
  const currentModule = MODULES[moduleKey];
  const title    = currentModule ? currentModule.title    : 'Panel Administrativo';
  const subtitle = currentModule ? currentModule.subtitle : 'Gestión integral de pedidos y entregas en tiempo real';

  return (
    <div className="admin-shell">
      <AdminSidebar onLogout={onLogout} />

      <main className="admin-content">
        <DashboardHeader
          title={title}
          subtitle={subtitle}
          user={session}
          wsStatus={wsStatus}
        />
        <Outlet />
      </main>
    </div>
  );
}
