import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '../components/auth/ProtectedRoute.jsx';
import AdminLayout    from '../layouts/AdminLayout.jsx';
import LoginPage      from '../pages/auth/LoginPage.jsx';
import AdminDashboard from '../pages/admin/AdminDashboard.jsx';
import ModulePage     from '../pages/admin/ModulePage.jsx';
import AccessDenied   from '../pages/shared/AccessDenied.jsx';
import LoadingPage    from '../pages/shared/LoadingPage.jsx';

const MODULES = [
  'pedidos', 'repartidores', 'pagos',
  'notificaciones', 'reclamaciones', 'reportes', 'configuracion',
];

export default function AppRouter({ session, checking, onLogin, onLogout }) {
  return (
    <Routes>
      {/* ── Login ── */}
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

      {/* ── Sin permiso ── */}
      <Route
        path="/sin-permiso"
        element={<AccessDenied onLogout={onLogout} />}
      />

      {/* ── Rutas protegidas ── */}
      <Route element={<ProtectedRoute session={session} checking={checking} />}>
        <Route element={<AdminLayout session={session} onLogout={onLogout} />}>
          <Route path="/admin" element={<AdminDashboard />} />
          {MODULES.map(key => (
            <Route key={key} path={`/admin/${key}`} element={<ModulePage module={key} />} />
          ))}
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
