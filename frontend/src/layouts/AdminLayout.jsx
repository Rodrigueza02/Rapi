/**
 * AdminLayout.jsx
 *
 * Shell del panel administrativo: sidebar + header + área de contenido (Outlet).
 *
 * CONEXIÓN WEBSOCKET
 * ------------------
 * Esta versión NO abre su propio WebSocket. Anteriormente abría una segunda
 * conexión (useWebSocket con callback noop) solo para mostrar el indicador de
 * estado en el header, lo que creaba una conexión duplicada innecesaria.
 *
 * El estado real del WebSocket lo gestiona cada página hija (AdminDashboard,
 * RepartidoresPage, etc.) a través de su propio useWebSocket. Para que el
 * header refleje ese estado, las páginas hijas lo comunican al layout mediante
 * el contexto de React Router (setWsStatus a través de useOutletContext).
 *
 * Si la página activa no reporta estado, el header muestra 'unknown'
 * (indicador neutro, sin color de error).
 */

import { useState, useCallback } from 'react';
import { Outlet, useLocation } from 'react-router-dom';

import AdminSidebar    from '../components/admin/AdminSidebar.jsx';
import DashboardHeader from '../components/admin/DashboardHeader.jsx';

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

  /* ── Estado del WebSocket: lo publican las páginas hijas ──────────────────
     Inicialmente 'unknown' (indicador neutro). AdminDashboard y otras páginas
     llaman setWsStatus (recibido por useOutletContext) cuando su useWebSocket
     cambia de estado, así el header siempre refleja la conexión real activa.  */
  const [wsStatus, setWsStatus] = useState('unknown');

  /* ── Estabilizar setWsStatus para que el Outlet context no cause re-renders */
  const reportWsStatus = useCallback((status) => setWsStatus(status), []);

  /* ── Título y subtítulo según la ruta ──────────────────────────────────── */
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
        {/* Las páginas hijas reciben reportWsStatus para actualizar el indicador */}
        <Outlet context={{ reportWsStatus }} />
      </main>
    </div>
  );
}
