import { useEffect, useState } from 'react';
import {
  BrowserRouter, Navigate, Outlet,
  Route, Routes, useNavigate, useLocation,
} from 'react-router-dom';

import { clearSession, readSession, verifySession } from './services/authService';
import ProtectedRoute  from './components/auth/ProtectedRoute';
import AdminSidebar    from './components/admin/AdminSidebar';
import DashboardHeader from './components/admin/DashboardHeader';
import LoginPage       from './pages/auth/LoginPage';
import AdminDashboard  from './pages/admin/AdminDashboard';
import ModulePage      from './pages/admin/ModulePage';

/** Mapa slug → título legible para el header del panel. */
const MODULE_NAMES = {
  pedidos:        'Pedidos',
  repartidores:   'Repartidores',
  pagos:          'Pagos',
  notificaciones: 'Notificaciones',
  reclamaciones:  'Reclamaciones',
  reportes:       'Reportes',
  configuracion:  'Configuración',
};

// ── Layout del panel admin ─────────────────────────────────────────────────────
function AdminLayout({ session, onLogout }) {
  const location = useLocation();
  const module   = location.pathname.split('/')[2];
  const title    = MODULE_NAMES[module] || 'Panel de control';

  return (
    <div className="admin-shell">
      <AdminSidebar onLogout={onLogout} />
      <main className="admin-content">
        <DashboardHeader title={title} user={session} />
        <Outlet />
      </main>
    </div>
  );
}

// ── Árbol de rutas ─────────────────────────────────────────────────────────────
function AppRoutes() {
  const [session,  setSession]  = useState(readSession);
  const [checking, setChecking] = useState(true);
  const navigate = useNavigate();

  /* Verifica el token contra el backend al arrancar la app */
  useEffect(() => {
    let active = true;
    verifySession(readSession()).then(s => {
      if (active) { setSession(s); setChecking(false); }
    });
    return () => { active = false; };
  }, []);

  function onLogin(s) {
    setSession(s);
    navigate(s.role === 'ADMIN' ? '/admin' : '/sin-permiso', { replace: true });
  }

  function onLogout() {
    clearSession();
    setSession(null);
    navigate('/login', { replace: true });
  }

  return (
    <Routes>
      {/* ── Login ── */}
      <Route
        path="/login"
        element={
          checking
            ? <div className="loading-page"><span className="loading-spinner" /></div>
            : session?.role === 'ADMIN'
              ? <Navigate to="/admin" replace />
              : <LoginPage onSuccess={onLogin} />
        }
      />

      {/* ── Sin permiso ── */}
      <Route
        path="/sin-permiso"
        element={
          <main className="access-denied">
            <h1>Acceso restringido</h1>
            <p>El panel administrativo requiere el rol ADMIN.</p>
            <button className="primary-button" onClick={onLogout}>
              Cerrar sesión
            </button>
          </main>
        }
      />

      {/* ── Rutas protegidas (solo ADMIN) ── */}
      <Route element={<ProtectedRoute session={session} checking={checking} />}>
        <Route element={<AdminLayout session={session} onLogout={onLogout} />}>
          <Route path="/admin" element={<AdminDashboard />} />
          {Object.keys(MODULE_NAMES).map(key => (
            <Route
              key={key}
              path={`/admin/${key}`}
              element={<ModulePage module={key} />}
            />
          ))}
        </Route>
      </Route>

      {/* ── Catch-all ── */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}
