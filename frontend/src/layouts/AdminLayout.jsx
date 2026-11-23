
import { Outlet, useLocation } from 'react-router-dom';

import AdminSidebar from '../components/admin/AdminSidebar.jsx';
import DashboardHeader from '../components/admin/DashboardHeader.jsx';

const MODULES = {
  pedidos: {
    title: 'Gestión de Pedidos',
    subtitle: 'Seguimiento y control de pedidos en tiempo real',
  },

  solicitudes: {
    title: 'Solicitudes de repartidores',
    subtitle: 'Gestiona, revisa y aprueba las solicitudes de nuevos repartidores para DeliveryFlow.',
  },

  repartidores: {
    title: 'Repartidores',
    subtitle: 'Gestión de la flota de repartidores y su disponibilidad en tiempo real',
  },

  pagos: {
    title: 'Pagos',
    subtitle: 'Gestión de cobros, transacciones y reembolsos',
  },

  correos: {
    title: 'Correos enviados',
    subtitle: 'Historial de notificaciones y comunicaciones',
  },
};

export default function AdminLayout({
  session,
  onLogout,
}) {
  const location = useLocation();

  const moduleKey = location.pathname.split('/')[2];

  const currentModule = MODULES[moduleKey];

  const title = currentModule
    ? currentModule.title
    : 'Panel Administrativo';

  const subtitle = currentModule
    ? currentModule.subtitle
    : 'Gestión integral de pedidos y entregas en tiempo real';

  return (
    <div className="admin-shell">
      <AdminSidebar onLogout={onLogout} />

      <main className="admin-content">
        <DashboardHeader
          title={title}
          subtitle={subtitle}
          user={session}
        />

        <Outlet />
      </main>
    </div>
  );
}
