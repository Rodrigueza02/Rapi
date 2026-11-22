/**
 * AdminLayout.jsx
 * SRP: única responsabilidad — estructurar el shell del panel administrativo
 * (sidebar + header + área de contenido).
 * No contiene lógica de sesión ni de rutas.
 */

import { Outlet, useLocation } from 'react-router-dom';
import AdminSidebar    from '../components/admin/AdminSidebar.jsx';
import DashboardHeader from '../components/admin/DashboardHeader.jsx';

const MODULE_NAMES = {
  pedidos:        'Pedidos',
  repartidores:   'Repartidores',
  pagos:          'Pagos',
  notificaciones: 'Notificaciones',
  reclamaciones:  'Reclamaciones',
  reportes:       'Reportes',
  configuracion:  'Configuración',
};

export default function AdminLayout({ session, onLogout }) {
  const location = useLocation();
  const module   = location.pathname.split('/')[2];
  const isHome   = !module;
  const title    = MODULE_NAMES[module] || 'Panel Administrativo';
  const subtitle = isHome
    ? 'Gestión integral de pedidos y entregas en tiempo real'
    : undefined;

  return (
    <div className="admin-shell">
      <AdminSidebar onLogout={onLogout} />
      <main className="admin-content">
        <DashboardHeader title={title} subtitle={subtitle} user={session} />
        <Outlet />
      </main>
    </div>
  );
}
