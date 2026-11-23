import { NavLink } from 'react-router-dom';

import {
  Home,
  ShoppingBag,
  Bell,
  Bike,
  CreditCard,
  Mail,
  LogOut,
  Truck,
} from 'lucide-react';

// Opciones del menú administrativo
const NAV_LINKS = [
  {
    to: '/admin',
    label: 'Inicio',
    icon: Home,
    end: true,
  },
  {
    to: '/admin/pedidos',
    label: 'Pedidos',
    icon: ShoppingBag,
  },
  {
    to: '/admin/solicitudes',
    label: 'Solicitudes',
    icon: Bell,
  },
  {
    to: '/admin/repartidores',
    label: 'Repartidores',
    icon: Bike,
  },
  {
    to: '/admin/pagos',
    label: 'Pagos',
    icon: CreditCard,
  },
  {
    to: '/admin/correos',
    label: 'Correos',
    icon: Mail,
  },
];

export default function AdminSidebar({ onLogout }) {
  return (
    <aside className="sidebar">

      {/* Logotipo de DeliveryFlow */}
      <div className="sidebar-brand">
        <Truck size={27} />

        <span>
          Delivery<b>Flow</b>
        </span>
      </div>

      {/* Encabezado del menú */}
      <p className="sidebar-label">
        MENÚ PRINCIPAL
      </p>

      {/* Opciones de navegación */}
      <nav className="sidebar-nav">
        {NAV_LINKS.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end ?? false}
              className={({ isActive }) =>
                `nav-item${isActive ? ' active' : ''}`
              }
            >
              <Icon size={19} />

              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* Cerrar sesión */}
      <button
        type="button"
        className="logout"
        onClick={onLogout}
      >
        <LogOut size={18} />
        <span>Cerrar sesión</span>
      </button>

    </aside>
  );
}
