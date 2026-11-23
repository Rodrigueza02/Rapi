import {
  Navigate,
  Route,
  Routes,
} from 'react-router-dom';

import ProtectedRoute from '../components/auth/ProtectedRoute.jsx';

import AdminLayout from '../layouts/AdminLayout.jsx';

import LoginPage from '../pages/auth/LoginPage.jsx';

import AdminDashboard from '../pages/admin/AdminDashboard.jsx';

import PedidosPage from '../pages/admin/PedidosPage.jsx';

import RepartidoresPage from '../pages/admin/RepartidoresPage.jsx';

import CorreosPage from '../pages/admin/CorreosPage.jsx';
import ModulePage from '../pages/admin/ModulePage.jsx';

import AccessDenied from '../pages/shared/AccessDenied.jsx';

import LoadingPage from '../pages/shared/LoadingPage.jsx';

const MODULES = [
  'pedidos',
  'solicitudes',
  'repartidores',
  'pagos',
  'correos',
];

export default function AppRouter({
  session,
  checking,
  onLogin,
  onLogout,
}) {
  return (
    <Routes>

      {/* Inicio de sesión */}
      <Route
        path="/login"
        element={
          checking
            ? <LoadingPage />
            : session?.role === 'ADMIN'
              ? <Navigate to="/admin" replace />
              : <LoginPage onSuccess={onLogin} />
        }
      />

      {/* Acceso denegado */}
      <Route
        path="/sin-permiso"
        element={
          <AccessDenied onLogout={onLogout} />
        }
      />

      {/* Rutas administrativas protegidas */}
      <Route
        element={
          <ProtectedRoute
            session={session}
            checking={checking}
          />
        }
      >
        <Route
          element={
            <AdminLayout
              session={session}
              onLogout={onLogout}
            />
          }
        >

          {/* Dashboard principal */}
          <Route
            path="/admin"
            element={<AdminDashboard />}
          />

          {/* Módulos administrativos */}
          {MODULES.map((module) => (
            <Route
              key={module}
              path={`/admin/${module}`}
              element={
                module === 'pedidos'
                  ? <PedidosPage />
                  : module === 'repartidores'
                    ? <RepartidoresPage />
                    : module === 'correos'
                      ? <CorreosPage />
                      : <ModulePage module={module} />
              }
            />
          ))}

          {/* Compatibilidad con rutas anteriores */}
          <Route path="/admin/notificaciones" element={<Navigate to="/admin/correos" replace />} />
          <Route path="/admin/reclamaciones" element={<Navigate to="/admin/correos" replace />} />
          <Route path="/admin/reportes" element={<Navigate to="/admin" replace />} />
          <Route path="/admin/configuracion" element={<Navigate to="/admin" replace />} />
          <Route path="/admin/solicitudes" element={<Navigate to="/admin" replace />} />
          <Route path="/admin/pagos" element={<Navigate to="/admin" replace />} />

        </Route>
      </Route>

      {/* Ruta no encontrada */}
      <Route
        path="*"
        element={
          <Navigate to="/login" replace />
        }
      />

    </Routes>
  );
}
