
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
    badge: 3,
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
      <div className="sidebar-brand">
        <Truck size={27} />
        <span>
          Delivery<b>Flow</b>
        </span>
      </div>

      <p className="sidebar-label">MENÚ PRINCIPAL</p>

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

              {item.badge && (
                <span className="nav-badge">
                  {item.badge}
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-bottom">
        <img
          src="/images/branding/repartidor-scooter.png"
          alt="Repartidor de DeliveryFlow"
          className="sidebar-illustration"
        />

        <button
          type="button"
          className="logout"
          onClick={onLogout}
        >
          <LogOut size={18} />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </aside>
  );
}
