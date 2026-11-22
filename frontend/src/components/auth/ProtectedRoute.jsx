import { Navigate, Outlet } from 'react-router-dom';

/**
 * Protege rutas que requieren rol ADMIN.
 *
 * - Mientras verifica la sesión muestra un spinner.
 * - Sin sesión redirige a /login.
 * - Con sesión pero sin rol ADMIN redirige a /sin-permiso.
 */
export default function ProtectedRoute({ session, checking }) {
  if (checking) {
    return (
      <div className="loading-page">
        <span className="loading-spinner" />
        Verificando sesión…
      </div>
    );
  }

  if (!session) return <Navigate to="/login" replace />;
  if (session.role !== 'ADMIN') return <Navigate to="/sin-permiso" replace />;

  return <Outlet />;
}
