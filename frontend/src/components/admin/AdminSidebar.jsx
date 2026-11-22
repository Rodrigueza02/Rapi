import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, ShoppingBag, Bike,
  CreditCard, Bell, MessageSquare,
  BarChart3, Settings, LogOut, Truck,
} from 'lucide-react';

const NAV_LINKS = [
  ['/admin',                'Resumen',        LayoutDashboard],
  ['/admin/pedidos',        'Pedidos',        ShoppingBag],
  ['/admin/repartidores',   'Repartidores',   Bike],
  ['/admin/pagos',          'Pagos',          CreditCard],
  ['/admin/notificaciones', 'Notificaciones', Bell],
  ['/admin/reclamaciones',  'Reclamaciones',  MessageSquare],
  ['/admin/reportes',       'Reportes',       BarChart3],
  ['/admin/configuracion',  'Configuración',  Settings],
];

export default function AdminSidebar({ onLogout }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <Truck size={27} />
        <span>Delivery<b>Flow</b></span>
      </div>

      <p className="sidebar-label">MENÚ PRINCIPAL</p>

      <nav className="sidebar-nav">
        {NAV_LINKS.map(([to, label, Icon]) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/admin'}
            className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}
          >
            <Icon size={19} />
            {label}
          </NavLink>
        ))}
      </nav>

      <button className="logout" onClick={onLogout}>
        <LogOut size={18} /> Cerrar sesión
      </button>
    </aside>
  );
}
